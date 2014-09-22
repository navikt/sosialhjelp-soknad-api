package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.lang.collections.iter.PreparedIterable;
import no.nav.modig.lang.collections.predicate.InstanceOf;
import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.Nokkel;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknadId;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadFaktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadVedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerConnector;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseConnector;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.meldinger.WSInnhold;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.Transformer;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.xml.bind.JAXBContext.newInstance;
import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.LASTET_OPP;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.equalTo;
import static no.nav.modig.lang.collections.PredicateUtils.not;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.toInnsendingsvalg;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.WebSoknadUtils.getJournalforendeEnhet;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.WebSoknadUtils.getSkjemanummer;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SoknadService implements SendSoknadService, EttersendingService {
    private static final Logger logger = getLogger(SoknadService.class);
    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository repository;

    @Inject
    @Named("vedleggRepository")
    private VedleggRepository vedleggRepository;
    @Inject
    private HenvendelseConnector henvendelseConnector;
    @Inject
    private FillagerConnector fillagerConnector;
    @Inject
    private Kodeverk kodeverk;
    @Inject
    private NavMessageSource navMessageSource;

    @Inject
    private StartDatoService startDatoService;

    private static final String EKSTRA_VEDLEGG_KEY = "ekstraVedlegg";
    private List<String> gyldigeSkjemaer = Arrays.asList("NAV 04-01.03");

    @Override
    public void settDelsteg(Long soknadId, DelstegStatus delstegStatus) {
        repository.settDelstegstatus(soknadId, delstegStatus);
    }

    @Override
    public WebSoknad hentSoknad(long soknadId) {
        return repository.hentSoknadMedData(soknadId);
    }

    @Override
    public String hentSoknadEier(Long soknadId) {
        return repository.hentSoknad(soknadId).getAktoerId();
    }

    @Override
    public Faktum lagreSoknadsFelt(Long soknadId, Faktum faktum) {
        faktum.setType(BRUKERREGISTRERT);
        faktum.setSoknadId(soknadId);
        Long faktumId = repository.lagreFaktum(soknadId, faktum);
        repository.settSistLagretTidspunkt(soknadId);

        settDelstegStatus(soknadId, faktum.getKey());

        Faktum resultat = repository.hentFaktum(soknadId, faktumId);
        genererVedleggForFaktum(resultat);
        on(repository.hentBarneFakta(soknadId, faktum.getFaktumId())).forEach(new Closure<Faktum>() {
            @Override
            public void execute(Faktum faktum) {
                genererVedleggForFaktum(faktum);
            }
        });

        return resultat;
    }

    @Override
    public void slettBrukerFaktum(Long soknadId, Long faktumId) {
        final Faktum faktum;
        try {
            faktum = repository.hentFaktum(soknadId, faktumId);
        } catch (IncorrectResultSizeDataAccessException e) {
            logger.info("Skipped delete because faktum does not exist.");
            return;
        }

        String faktumKey = faktum.getKey();
        List<Vedlegg> vedleggliste = vedleggRepository.hentVedleggForFaktum(soknadId, faktumId);

        for (Vedlegg vedlegg : vedleggliste) {
            vedleggRepository.slettVedleggOgData(soknadId, vedlegg.getFaktumId(), vedlegg.getSkjemaNummer());
        }
        repository.slettBrukerFaktum(soknadId, faktumId);
        repository.settSistLagretTidspunkt(soknadId);
        settDelstegStatus(soknadId, faktumKey);
    }

    @Override
    public Long lagreSystemFaktum(Long soknadId, Faktum f, String uniqueProperty) {
        logger.debug("*** Lagrer systemfaktum ***: " + f.getKey());
        f.setType(SYSTEMREGISTRERT);
        List<Faktum> fakta = repository.hentSystemFaktumList(soknadId, f.getKey());

        if (!uniqueProperty.isEmpty()) {
            for (Faktum faktum : fakta) {
                if (faktum.matcherUnikProperty(uniqueProperty, f)) {
                    f.setFaktumId(faktum.getFaktumId());
                    Long lagretFaktumId = repository.lagreFaktum(soknadId, f, true);
                    Faktum hentetFaktum = repository.hentFaktum(soknadId, lagretFaktumId);
                    genererVedleggForFaktum(hentetFaktum);
                    return lagretFaktumId;
                }
            }
        }
        Long lagretFaktumId = repository.lagreFaktum(soknadId, f, true);
        Faktum hentetFaktum = repository.hentFaktum(soknadId, lagretFaktumId);
        genererVedleggForFaktum(hentetFaktum);

        repository.settSistLagretTidspunkt(soknadId);
        return lagretFaktumId;
    }

    public WebSoknad hentSoknadMedBehandlingsId(String behandlingsId) {
        WebSoknad soknad = repository.hentMedBehandlingsId(behandlingsId);
        if (soknad == null) {
            Map<String, Object> map = populerFraHenvendelse(behandlingsId);
            SoknadInnsendingStatus status = (SoknadInnsendingStatus) map.get("status");
            if (status.equals(UNDER_ARBEID)) {
                soknad = repository.hentMedBehandlingsId(behandlingsId);
            } else {
                soknad = new WebSoknad()
                        .medskjemaNummer((String) map.get("skjemanummer"))
                        .medStatus(status);
            }
        }
        return soknad;
    }

    private void settDelstegStatus(Long soknadId, String faktumKey) {
        //Setter delstegstatus dersom et faktum blir lagret, med mindre det er epost eller ekstra vedlegg. Bør gjøres mer elegant, litt quickfix
        if (!Personalia.EPOST_KEY.equals(faktumKey) && !EKSTRA_VEDLEGG_KEY.equals(faktumKey)) {
            repository.settDelstegstatus(soknadId, DelstegStatus.UTFYLLING);
        }
    }

    private Map<String, Object> populerFraHenvendelse(String behandlingsId) {
        Map<String, Object> returnMap = new HashMap<>();
        WSHentSoknadResponse wsSoknadsdata = henvendelseConnector.hentSoknad(behandlingsId);

        XMLMetadataListe vedleggListe = (XMLMetadataListe) wsSoknadsdata.getAny();
        Optional<XMLMetadata> hovedskjema = on(vedleggListe.getMetadata()).filter(new InstanceOf<XMLMetadata>(XMLHovedskjema.class)).head();
        if (!hovedskjema.isSome()) {
            throw new ApplicationException("Kunne ikke hente opp søknad");
        }

        SoknadInnsendingStatus status = SoknadInnsendingStatus.valueOf(wsSoknadsdata.getStatus());
        if (status.equals(UNDER_ARBEID)) {
            byte[] bytes = fillagerConnector.hentFil(((XMLHovedskjema) hovedskjema.get()).getUuid());
            WebSoknad soknad = JAXB.unmarshal(new ByteArrayInputStream(bytes), WebSoknad.class);
            repository.populerFraStruktur(soknad);
            List<WSInnhold> innhold = fillagerConnector.hentFiler(soknad.getBrukerBehandlingId());
            populerVedleggMedDataFraHenvendelse(soknad, innhold);
        }

        returnMap.put("skjemanummer", ((XMLHovedskjema) hovedskjema.get()).getSkjemanummer());
        returnMap.put("status", status);

        return returnMap;
    }

    private void populerVedleggMedDataFraHenvendelse(WebSoknad soknad, List<WSInnhold> innhold) {
        for (WSInnhold wsInnhold : innhold) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                wsInnhold.getInnhold().writeTo(baos);
            } catch (IOException e) {
                throw new ApplicationException("Kunne ikke hente opp soknaddata", e);
            }
            Vedlegg vedlegg = soknad.hentVedleggMedUID(wsInnhold.getUuid());
            if (vedlegg != null) {
                vedlegg.setData(baos.toByteArray());
                vedleggRepository.lagreVedleggMedData(soknad.getSoknadId(), vedlegg.getVedleggId(), vedlegg);
            }
        }
    }

    @Override
    public Map<String, String> hentInnsendtDatoForOpprinneligSoknad(String behandlingsId) {
        Map<String, String> result = new HashMap<>();
        List<WSBehandlingskjedeElement> wsBehandlingskjedeElements = henvendelseConnector.hentBehandlingskjede(behandlingsId);
        List<WSBehandlingskjedeElement> sorterteBehandlinger = on(wsBehandlingskjedeElements).filter(where(STATUS, (equalTo(SoknadInnsendingStatus.FERDIG)))).collect(new Comparator<WSBehandlingskjedeElement>() {
            @Override
            public int compare(WSBehandlingskjedeElement o1, WSBehandlingskjedeElement o2) {
                DateTime dato1 = o1.getInnsendtDato();
                DateTime dato2 = o2.getInnsendtDato();

                if (dato1 == null && dato2 == null) {
                    return 0;
                } else if (dato1 == null) {
                    return 1;
                } else if (dato2 == null) {
                    return -1;
                }
                return dato1.compareTo(dato2);
            }
        });

        WSBehandlingskjedeElement innsendtSoknad = sorterteBehandlinger.get(0);
        result.put("innsendtdato",String.valueOf(innsendtSoknad.getInnsendtDato().getMillis()));
        result.put("sisteinnsendtbehandling", sorterteBehandlinger.get(sorterteBehandlinger.size()-1).getBehandlingsId().toString());
        return result;
    }

    @Override
    public WebSoknad hentEttersendingForBehandlingskjedeId(String behandlingsId) {
        Optional<WebSoknad> soknadOptional = repository.hentEttersendingMedBehandlingskjedeId(behandlingsId);
        if (soknadOptional.isSome()) {
            return soknadOptional.get();
        } else {
            return null;
        }
    }

    @Override
    public Long startEttersending(String behandingsId) {
        List<WSBehandlingskjedeElement> behandlingskjede = henvendelseConnector.hentBehandlingskjede(behandingsId);
        WSHentSoknadResponse wsSoknadsdata = hentSisteIkkeAvbrutteSoknadIBehandlingskjede(behandlingskjede);

        if(wsSoknadsdata.getInnsendtDato() == null) {
            throw new ApplicationException("Kan ikke starte ettersending på en ikke fullfort soknad");
        }
        DateTime innsendtDato = hentOrginalInnsendtDato(behandlingskjede, behandingsId);
        return lagEttersendingFraWsSoknad(wsSoknadsdata, innsendtDato).getSoknadId();
    }

    private DateTime hentOrginalInnsendtDato(List<WSBehandlingskjedeElement> behandlingskjede, String behandlingsId) {
        return on(behandlingskjede)
                .filter(where(BEHANDLINGS_ID, equalTo(behandlingsId)))
                .head()
                .get()
                .getInnsendtDato();
    }

    private WSHentSoknadResponse hentSisteIkkeAvbrutteSoknadIBehandlingskjede(List<WSBehandlingskjedeElement> behandlingskjede) {
        List<WSBehandlingskjedeElement> sorterteBehandlinger = on(behandlingskjede).filter(where(STATUS, not(equalTo(SoknadInnsendingStatus.AVBRUTT_AV_BRUKER)))).collect(new Comparator<WSBehandlingskjedeElement>() {
            @Override
            public int compare(WSBehandlingskjedeElement o1, WSBehandlingskjedeElement o2) {
                DateTime dato1 = o1.getInnsendtDato();
                DateTime dato2 = o2.getInnsendtDato();
                if (dato1 == null && dato2 == null) {
                    return 0;
                } else if (dato1 == null) {
                    return -1;
                } else if (dato2 == null) {
                    return 1;
                }
                return dato2.compareTo(dato1);
            }
        });

        return henvendelseConnector.hentSoknad(sorterteBehandlinger.get(0).getBehandlingsId());
    }

    private WebSoknad lagEttersendingFraWsSoknad(WSHentSoknadResponse opprinneligInnsending, DateTime innsendtDato) {
        String ettersendingsBehandlingId = henvendelseConnector.startEttersending(opprinneligInnsending);
        WSHentSoknadResponse wsEttersending = henvendelseConnector.hentSoknad(ettersendingsBehandlingId);

        String behandlingskjedeId;
        if(opprinneligInnsending.getBehandlingskjedeId() != null) {
            behandlingskjedeId = opprinneligInnsending.getBehandlingskjedeId();
        } else {
            behandlingskjedeId = opprinneligInnsending.getBehandlingsId();
        }

        WebSoknad soknad = WebSoknad.startEttersending(ettersendingsBehandlingId);
        String mainUid = randomUUID().toString();
        XMLMetadataListe xmlVedleggListe = (XMLMetadataListe) wsEttersending.getAny();
        Optional<XMLMetadata> hovedskjema = on(xmlVedleggListe.getMetadata()).filter(new InstanceOf<XMLMetadata>(XMLHovedskjema.class)).head();
        if (!hovedskjema.isSome()) {
            throw new ApplicationException("Kunne ikke hente opp hovedskjema for søknad");
        }
        XMLHovedskjema xmlHovedskjema = (XMLHovedskjema) hovedskjema.get();

        soknad.medUuid(mainUid)
                .medAktorId(getSubjectHandler().getUid())
                .medskjemaNummer(xmlHovedskjema.getSkjemanummer())
                .medBehandlingskjedeId(behandlingskjedeId)
                .medJournalforendeEnhet(xmlHovedskjema.getJournalforendeEnhet());

        Long soknadId = repository.opprettSoknad(soknad);
        WebSoknadId websoknadId = new WebSoknadId();
        websoknadId.setId(soknadId);
        soknad.setSoknadId(soknadId);

        Faktum soknadInnsendingsDato = new Faktum()
                .medSoknadId(soknadId)
                .medKey("soknadInnsendingsDato")
                .medValue(String.valueOf(innsendtDato.getMillis()))
                .medType(SYSTEMREGISTRERT);
        lagreSystemFaktum(soknadId, soknadInnsendingsDato, "");
        soknad.setFaktaListe(repository.hentAlleBrukerData(soknadId));

        soknad.setVedlegg(hentVedleggOgPersister(xmlVedleggListe, soknadId));

        return soknad;
    }

    @Override
    public void sendSoknad(long soknadId, byte[] pdf) {
        WebSoknad soknad = hentSoknad(soknadId);

        if (soknad.erEttersending() && soknad.getOpplastedeVedlegg().size() <= 0) {
            logger.error("Kan ikke sende inn ettersendingen med ID {0} uten å ha lastet opp vedlegg", soknad.getBrukerBehandlingId());
            throw new ApplicationException(String.format("Kan ikke sende inn ettersendingen uten å ha lastet opp vedlegg"));
        }

        if (soknad.harAnnetVedleggSomIkkeErLastetOpp()) {
            logger.error("Kan ikke sende inn behandling (ID: {0}) med Annet vedlegg (skjemanummer N6) som ikke er lastet opp", soknad.getBrukerBehandlingId());
            throw new ApplicationException(String.format("Kan ikke sende inn behandling uten å ha lastet opp alle  vedlegg med skjemanummer N6"));
        }

        logger.info("Lagrer søknad som fil til henvendelse for behandling {}", soknad.getBrukerBehandlingId());
        fillagerConnector.lagreFil(soknad.getBrukerBehandlingId(), soknad.getUuid(), soknad.getAktoerId(), new ByteArrayInputStream(pdf));

        List<Vedlegg> vedleggForventnings = soknad.getVedlegg();

        String skjemanummer = getSkjemanummer(soknad);
        String journalforendeEnhet = getJournalforendeEnhet(soknad);
        XMLHovedskjema hovedskjema = new XMLHovedskjema()
                .withInnsendingsvalg(LASTET_OPP.toString())
                .withSkjemanummer(skjemanummer)
                .withFilnavn(skjemanummer)
                .withMimetype("application/pdf")
                .withFilstorrelse("" + pdf.length)
                .withUuid(soknad.getUuid())
                .withJournalforendeEnhet(journalforendeEnhet);
        henvendelseConnector.avsluttSoknad(soknad.getBrukerBehandlingId(),
                hovedskjema,
                Transformers.convertToXmlVedleggListe(vedleggForventnings));
        repository.slettSoknad(soknadId);
    }

    private List<Vedlegg> hentVedleggOgPersister(XMLMetadataListe xmlVedleggListe, Long soknadId) {
        PreparedIterable<XMLMetadata> vedlegg = on(xmlVedleggListe.getMetadata()).filter(new InstanceOf<XMLMetadata>(XMLVedlegg.class));
        List<Vedlegg> soknadVedlegg = new ArrayList<>();
        for (XMLMetadata xmlMetadata : vedlegg) {
            if (xmlMetadata instanceof XMLHovedskjema) {
                continue;
            }
            XMLVedlegg xmlVedlegg = (XMLVedlegg) xmlMetadata;

            Integer antallSider = xmlVedlegg.getSideantall() != null ? xmlVedlegg.getSideantall() : 0;

            Vedlegg v = new Vedlegg()
                    .medSkjemaNummer(xmlVedlegg.getSkjemanummer())
                    .medAntallSider(antallSider)
                    .medInnsendingsvalg(toInnsendingsvalg(xmlVedlegg.getInnsendingsvalg()))
                    .medOpprinneligInnsendingsvalg(toInnsendingsvalg(xmlVedlegg.getInnsendingsvalg()))
                    .medSoknadId(soknadId)
                    .medNavn(xmlVedlegg.getTilleggsinfo());

            String skjemanummerTillegg = xmlVedlegg.getSkjemanummerTillegg();
            if (isNotBlank(skjemanummerTillegg)) {
                v.setSkjemaNummer(v.getSkjemaNummer() + "|" + skjemanummerTillegg);
            }

            medKodeverk(v);
            vedleggRepository.opprettVedlegg(v, null);
            soknadVedlegg.add(v);
        }
        return soknadVedlegg;
    }

    @Override
    public void avbrytSoknad(Long soknadId) {
        WebSoknad soknad = repository.hentSoknad(soknadId);

        /**
        * Sletter alle vedlegg til søknader som blir avbrutt.
        * Dette burde egentlig gjøres i henvendelse, siden vi uansett skal slette alle vedlegg på avbrutte søknader.
        * I tillegg blir det liggende igjen mange vedlegg for søknader som er avbrutt før dette kallet ble lagt til.
        * */

        fillagerConnector.slettAlle(soknad.getBrukerBehandlingId());
        henvendelseConnector.avbrytSoknad(soknad.getBrukerBehandlingId());
        repository.slettSoknad(soknadId);
    }

    @Override
    public List<Faktum> hentFakta(Long soknadId) {
        return repository.hentAlleBrukerData(soknadId);
    }

    @Override
    public String startSoknad(String navSoknadId) {
        validerSkjemanummer(navSoknadId);

        String mainUid = randomUUID().toString();
        String behandlingsId = henvendelseConnector
                .startSoknad(getSubjectHandler().getUid(), navSoknadId, mainUid);
        WebSoknad soknad = WebSoknad.startSoknad()
                .medBehandlingId(behandlingsId).medskjemaNummer(navSoknadId)
                .medUuid(mainUid)
                .medAktorId(getSubjectHandler().getUid())
                .medOppretteDato(DateTime.now());

        Long soknadId = repository.opprettSoknad(soknad);
        WebSoknadId websoknadId = new WebSoknadId();
        websoknadId.setId(soknadId);

        List<String> bolker = Arrays.asList("reellarbeidssoker", "arbeidsforhold", "egennaering", "verneplikt", "utdanning", "ytelser", "personalia", "barnetillegg", "fritekst");
        Map<String, String> erBolkerValidert = new HashMap<>();
        for (String bolk : bolker) {
            erBolkerValidert.put(bolk, "false");
        }

        Faktum bolkerFaktum = new Faktum().medSoknadId(soknadId).medKey("bolker").medType(BRUKERREGISTRERT);
        bolkerFaktum.setProperties(erBolkerValidert);

        repository.lagreFaktum(soknadId, bolkerFaktum);
        prepopulerSoknadsFakta(soknadId);

        opprettFaktumForLonnsOgTrekkoppgave(soknadId);

        return behandlingsId;
    }

    private void opprettFaktumForLonnsOgTrekkoppgave(Long soknadId) {
        if (startDatoService.erJanuarEllerFebruar()) {
            Faktum lonnsOgTrekkoppgaveFaktum = new Faktum()
                    .medSoknadId(soknadId)
                    .medKey("lonnsOgTrekkOppgave")
                    .medType(SYSTEMREGISTRERT)
                    .medValue("true");
            lagreSystemFaktum(soknadId, lonnsOgTrekkoppgaveFaktum, "");
        }
    }

    private void prepopulerSoknadsFakta(Long soknadId) {
        SoknadStruktur soknadStruktur = hentSoknadStruktur(soknadId);
        List<SoknadFaktum> fakta = soknadStruktur.getFakta();

        Collections.sort(fakta, SoknadFaktum.sammenlignEtterDependOn());

        for (SoknadFaktum soknadFaktum : fakta) {
            String flereTillatt = soknadFaktum.getFlereTillatt();
            String erSystemFaktum = soknadFaktum.getErSystemFaktum();
            if((flereTillatt != null && flereTillatt.equals("true")) || (erSystemFaktum != null && erSystemFaktum.equals("true"))) {
                continue;
            }

            Faktum f = new Faktum()
                    .medKey(soknadFaktum.getId())
                    .medValue("")
                    .medType(Faktum.FaktumType.BRUKERREGISTRERT);

            if (soknadFaktum.getDependOn() != null) {
                Faktum parentFaktum = repository.hentFaktumMedKey(soknadId, soknadFaktum.getDependOn().getId());
                f.setParrentFaktum(parentFaktum.getFaktumId());
            }

            repository.lagreFaktum(soknadId,f);
        }
    }

    private void validerSkjemanummer(String navSoknadId) {
        if (!gyldigeSkjemaer.contains(navSoknadId)) {
            throw new ApplicationException("Ikke gyldig skjemanummer " + navSoknadId);
        }
    }

    @Override
    public SoknadStruktur hentSoknadStruktur(Long soknadId) {
        return hentStruktur(repository.hentSoknadType(soknadId));
    }

    @Override
    public SoknadStruktur hentSoknadStruktur(String skjemaNummer) {
        return hentStruktur(skjemaNummer);
    }

    private void genererVedleggForFaktum(Faktum faktum) {
        SoknadStruktur struktur = hentSoknadStruktur(faktum.getSoknadId());
        List<SoknadVedlegg> aktuelleVedlegg = struktur.vedleggFor(faktum.getKey());
        for (SoknadVedlegg soknadVedlegg : aktuelleVedlegg) {
            Vedlegg vedlegg = vedleggRepository.hentVedleggForskjemaNummer(faktum.getSoknadId(), soknadVedlegg.getFlereTillatt() ? faktum.getFaktumId() : null, soknadVedlegg.getSkjemaNummer());
            Faktum parentFaktum = faktum.getParrentFaktum() != null ? repository.hentFaktum(faktum.getSoknadId(), faktum.getParrentFaktum()) : null;
            if (soknadVedlegg.trengerVedlegg(faktum) && erParentAktiv(soknadVedlegg, parentFaktum)) {
                lagrePaakrevdVedlegg(faktum, soknadVedlegg, vedlegg);
            } else if (vedlegg != null && !erVedleggKrevdAvAnnetFaktum(faktum, struktur, soknadVedlegg)) { // sett vedleggsforventning til ikke paakrevd
                vedlegg.setInnsendingsvalg(Vedlegg.Status.IkkeVedlegg);
                vedleggRepository.lagreVedlegg(faktum.getSoknadId(), vedlegg.getVedleggId(), vedlegg);
            }
        }
    }

    private boolean erVedleggKrevdAvAnnetFaktum(Faktum faktum,
                                                SoknadStruktur struktur, SoknadVedlegg soknadVedlegg) {
        return !soknadVedlegg.getFlereTillatt() && annetFaktumHarForventning(faktum.getSoknadId(), soknadVedlegg.getSkjemaNummer(), soknadVedlegg.getOnValue(), struktur);
    }

    private void lagrePaakrevdVedlegg(Faktum faktum, SoknadVedlegg soknadVedlegg, Vedlegg v) {
        Vedlegg vedlegg = v;
        if (vedlegg == null) {
            vedlegg = new Vedlegg(faktum.getSoknadId(), soknadVedlegg.getFlereTillatt() ? faktum.getFaktumId() : null, soknadVedlegg.getSkjemaNummer(), Vedlegg.Status.VedleggKreves);
            vedlegg.setVedleggId(vedleggRepository.opprettVedlegg(vedlegg, null));
        }
        vedlegg.oppdatertInnsendtStatus();

        if (soknadVedlegg.getProperty() != null && faktum.getProperties().containsKey(soknadVedlegg.getProperty())) {
            vedlegg.setNavn(faktum.getProperties().get(soknadVedlegg.getProperty()));
        } else if (soknadVedlegg.harOversetting()) {
            vedlegg.setNavn(navMessageSource.getMessage(soknadVedlegg.getOversetting().replace("${key}", faktum.getKey()), new Object[0], new Locale("nb", "NO")));
        }
        vedleggRepository.lagreVedlegg(faktum.getSoknadId(), vedlegg.getVedleggId(), vedlegg);
    }

    private boolean erParentAktiv(SoknadVedlegg soknadVedlegg, Faktum parent) {
        return parent == null || erParentValueNullOgVedleggDependOnFalse(soknadVedlegg, parent) || parentValueErLikDependOnVerdi(soknadVedlegg, parent);
    }

    private boolean parentValueErLikDependOnVerdi(SoknadVedlegg soknadVedlegg, Faktum parent) {
        return parent.getValue().equals(soknadVedlegg.getFaktum().getDependOnValue());
    }
    
    private boolean erParentValueNullOgVedleggDependOnFalse(SoknadVedlegg soknadVedlegg, Faktum parent) {
        return parent.getValue() == null && "false".equalsIgnoreCase(soknadVedlegg.getFaktum().getDependOnValue());
    }

    /**
     * Looper alle mulige vedleggsforventinger for gitt skjemanummer,
     * dersom soknadbrukerdata har et innslag som har riktig onValue, returneres true (et annet faktum trigger vedlegget)
     * ellers returneres false
     */
    private boolean annetFaktumHarForventning(Long soknadId, String skjemaNummer, String onValue, SoknadStruktur struktur) {
        List<SoknadVedlegg> vedleggMedGittSkjemanummer = struktur.vedleggForSkjemanr(skjemaNummer);
        for (SoknadVedlegg sv : vedleggMedGittSkjemanummer) {

            String faktumKey = sv.getFaktum().getId();
            if (repository.isVedleggPaakrevd(soknadId, faktumKey, onValue, sv.getFaktum().getDependOnValue())) {
                return true;
            }
        }
        return false;
    }

    private void medKodeverk(Vedlegg vedlegg) {
        try {
            Map<Kodeverk.Nokkel, String> koder = kodeverk.getKoder(vedlegg.getSkjemaNummerFiltrert());
            for (Entry<Nokkel, String> nokkelEntry : koder.entrySet()) {
                if (nokkelEntry.getKey().toString().contains("URL")) {
                    vedlegg.leggTilURL(nokkelEntry.getKey().toString(), koder.get(nokkelEntry.getKey()));
                }
            }
            vedlegg.setTittel(koder.get(Kodeverk.Nokkel.TITTEL));
        } catch (Exception ignore) {
            logger.debug("ignored exception");

        }
    }

    private SoknadStruktur hentStruktur(String skjema) {
        //TODO: Få flyttet dette ut på et vis? Ta i bruk.
        Map<String,String> strukturDokumenter =  new HashMap<>();
        strukturDokumenter.put("NAV 04-01.04", "NAV 04-01.03.xml");
        strukturDokumenter.put("NAV 04-01.03", "NAV 04-01.03.xml");

        String type = strukturDokumenter.get(skjema);

        if(type == null || type.isEmpty()) {
            throw new ApplicationException("Fant ikke strukturdokument for nav-skjemanummer: " + skjema);
        }

        try {
            Unmarshaller unmarshaller = newInstance(SoknadStruktur.class)
                    .createUnmarshaller();
            return (SoknadStruktur) unmarshaller.unmarshal(SoknadStruktur.class
                    .getResourceAsStream(format("/soknader/%s", type)));
        } catch (JAXBException e) {
            throw new RuntimeException("Kunne ikke laste definisjoner. ", e);
        }
    }

    private static final Transformer<WSBehandlingskjedeElement, SoknadInnsendingStatus> STATUS = new Transformer<WSBehandlingskjedeElement, SoknadInnsendingStatus>() {
        public SoknadInnsendingStatus transform(WSBehandlingskjedeElement input) {
            return SoknadInnsendingStatus.valueOf(input.getStatus());
        }
    };

    private static final Transformer<WSBehandlingskjedeElement, String> BEHANDLINGS_ID = new Transformer<WSBehandlingskjedeElement, String>() {
        public String transform(WSBehandlingskjedeElement input) {
            return input.getBehandlingsId();
        }
    };
}