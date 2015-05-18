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
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadFaktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.meldinger.WSInnhold;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import static java.util.UUID.randomUUID;
import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.LASTET_OPP;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.*;
import static no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.KVITTERING;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.toInnsendingsvalg;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.PersonaliaUtils.adresserOgStatsborgerskap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SoknadService implements SendSoknadService, EttersendingService {

    private static final Logger logger = getLogger(SoknadService.class);
    private static final String AAP_INTERNASJONAL = "2101";

    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository repository;

    @Inject
    @Named("vedleggRepository")
    private VedleggRepository vedleggRepository;

    @Inject
    private HenvendelseService henvendelseService;

    @Inject
    private FillagerService fillagerService;

    @Inject
    private Kodeverk kodeverk;

    @Inject
    private StartDatoService startDatoService;

    @Inject
    private FaktaService faktaService;

    @Inject
    public ApplicationContext applicationContex;

    @Inject
    private WebSoknadConfig config;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    private Map<String, BolkService> bolker;

    @PostConstruct
    public void initBolker() {
        bolker = applicationContex.getBeansOfType(BolkService.class);
    }


    public void settDelsteg(String behandlingsId, DelstegStatus delstegStatus) {
        repository.settDelstegstatus(behandlingsId, delstegStatus);
    }

    public void settJournalforendeEnhet(String behandlingsId, String journalforendeEnhet) {
        repository.settJournalforendeEnhet(behandlingsId, journalforendeEnhet);
    }

    public WebSoknad hentSoknad(long soknadId) {
        return repository.hentSoknad(soknadId);
    }

    public WebSoknad hentSoknad(String behandlingsId) {
        WebSoknad soknad = hentSoknadFraDbEllerHenvendelse(behandlingsId);
        soknad.medSoknadPrefix(config.getSoknadTypePrefix(soknad.getSoknadId()))
                .medSoknadUrl(config.getSoknadUrl(soknad.getSoknadId()))
                .medFortsettSoknadUrl(config.getFortsettSoknadUrl(soknad.getSoknadId()));

        oppdaterKjentInformasjon(getSubjectHandler().getUid(), soknad);

        return soknad;
    }

    public WebSoknad hentSoknadForTilgangskontroll(String behandlingsId) {
        return hentSoknadFraDbEllerHenvendelse(behandlingsId);
    }

    //to do: bare ta inn behandlingsid videre
    public WebSoknad hentSoknadMedFaktaOgVedlegg(String behandlingsId) {
        WebSoknad soknad = repository.hentSoknadMedData(behandlingsId);
        if (soknad == null) {
            soknad = hentFraHenvendelse(behandlingsId, true);
        }
        soknad.medSoknadPrefix(config.getSoknadTypePrefix(soknad.getSoknadId()))
                .medSoknadUrl(config.getSoknadUrl(soknad.getSoknadId()))
                .medFortsettSoknadUrl(config.getFortsettSoknadUrl(soknad.getSoknadId()));
        return soknad;
    }

    private WebSoknad hentSoknadFraDbEllerHenvendelse(String behandlingsId) {
        WebSoknad soknad = repository.hentSoknad(behandlingsId);
        if (soknad == null) {
            soknad = hentFraHenvendelse(behandlingsId, false);
        }
        return soknad;
    }

    private WebSoknad hentFraHenvendelse(String behandlingsId, boolean medFaktumOgVedlegg) {
        WSHentSoknadResponse wsSoknadsdata = henvendelseService.hentSoknad(behandlingsId);

        XMLMetadataListe vedleggListe = (XMLMetadataListe) wsSoknadsdata.getAny();
        Optional<XMLMetadata> hovedskjemaOptional = on(vedleggListe.getMetadata()).filter(new InstanceOf<XMLMetadata>(XMLHovedskjema.class)).head();
        XMLHovedskjema hovedskjema = (XMLHovedskjema) hovedskjemaOptional.getOrThrow(new ApplicationException("Kunne ikke hente opp søknad"));

        WebSoknad soknad;
        SoknadInnsendingStatus status = SoknadInnsendingStatus.valueOf(wsSoknadsdata.getStatus());
        if (status.equals(UNDER_ARBEID)) {
            populerSoknadFraHenvendelse(hovedskjema);
            if (medFaktumOgVedlegg) {
                soknad = repository.hentSoknadMedData(behandlingsId);
            } else {
                soknad = repository.hentSoknad(behandlingsId);
            }
        } else {
            // søkndadsdata er slettet i henvendelse, har kun metadata
            soknad = new WebSoknad().medBehandlingId(behandlingsId).medStatus(status).medskjemaNummer(hovedskjema.getSkjemanummer());
        }

        return soknad;
    }

    private void populerSoknadFraHenvendelse(XMLHovedskjema hovedskjema) {
        byte[] bytes = fillagerService.hentFil(hovedskjema.getUuid());
        WebSoknad soknad = JAXB.unmarshal(new ByteArrayInputStream(bytes), WebSoknad.class);
        repository.populerFraStruktur(soknad);
        List<WSInnhold> innhold = fillagerService.hentFiler(soknad.getBrukerBehandlingId());
        populerVedleggMedDataFraHenvendelse(soknad, innhold);
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
    public Map<String, String> hentInnsendtDatoOgSisteInnsending(String behandlingsId) {
        Map<String, String> result = new HashMap<>();
        List<WSBehandlingskjedeElement> wsBehandlingskjedeElements = henvendelseService.hentBehandlingskjede(behandlingsId);
        List<WSBehandlingskjedeElement> sorterteBehandlinger =
                on(wsBehandlingskjedeElements).filter(where(STATUS, (equalTo(SoknadInnsendingStatus.FERDIG)))).collect(SORTER_INNSENDT_DATO);

        WSBehandlingskjedeElement innsendtSoknad = sorterteBehandlinger.get(0);
        result.put("innsendtdatoSoknad", String.valueOf(innsendtSoknad.getInnsendtDato().getMillis()));
        result.put("sistInnsendteBehandlingsId", sorterteBehandlinger.get(sorterteBehandlinger.size() - 1).getBehandlingsId());
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
    public String startEttersending(String behandlingsIdSoknad, String fodselsnummer) {
        List<WSBehandlingskjedeElement> behandlingskjede = henvendelseService.hentBehandlingskjede(behandlingsIdSoknad);
        WSHentSoknadResponse wsSoknadsdata = hentSisteIkkeAvbrutteSoknadIBehandlingskjede(behandlingskjede);

        if (wsSoknadsdata.getInnsendtDato() == null) {
            throw new ApplicationException("Kan ikke starte ettersending på en ikke fullfort soknad");
        }
        DateTime innsendtDato = hentOrginalInnsendtDato(behandlingskjede, behandlingsIdSoknad);
        WebSoknad ettersending = lagEttersendingFraWsSoknad(wsSoknadsdata, innsendtDato);
        return ettersending.getBrukerBehandlingId();
    }

    private DateTime hentOrginalInnsendtDato(List<WSBehandlingskjedeElement> behandlingskjede, String behandlingsId) {
        return on(behandlingskjede)
                .filter(where(BEHANDLINGS_ID, equalTo(behandlingsId)))
                .head()
                .get()
                .getInnsendtDato();
    }

    private WSHentSoknadResponse hentSisteIkkeAvbrutteSoknadIBehandlingskjede(List<WSBehandlingskjedeElement> behandlingskjede) {
        List<WSBehandlingskjedeElement> sorterteBehandlinger = on(behandlingskjede).filter(where(STATUS, not(equalTo(SoknadInnsendingStatus.AVBRUTT_AV_BRUKER))))
                .collect(new Comparator<WSBehandlingskjedeElement>() {
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

        return henvendelseService.hentSoknad(sorterteBehandlinger.get(0).getBehandlingsId());
    }

    private WebSoknad lagEttersendingFraWsSoknad(WSHentSoknadResponse opprinneligInnsending, DateTime innsendtDato) {
        String ettersendingsBehandlingId = henvendelseService.startEttersending(opprinneligInnsending);
        WSHentSoknadResponse wsEttersending = henvendelseService.hentSoknad(ettersendingsBehandlingId);

        String behandlingskjedeId;
        if (opprinneligInnsending.getBehandlingskjedeId() != null) {
            behandlingskjedeId = opprinneligInnsending.getBehandlingskjedeId();
        } else {
            behandlingskjedeId = opprinneligInnsending.getBehandlingsId();
        }

        WebSoknad soknad = WebSoknad.startEttersending(ettersendingsBehandlingId);
        String mainUid = randomUUID().toString();
        List<XMLMetadata> xmlVedleggListe = ((XMLMetadataListe) wsEttersending.getAny()).getMetadata();
        List<XMLMetadata> filtrertXmlVedleggListe = on(xmlVedleggListe).filter(not(kvittering())).collect();

        Optional<XMLMetadata> hovedskjema = on(filtrertXmlVedleggListe).filter(new InstanceOf<XMLMetadata>(XMLHovedskjema.class)).head();
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
        soknad.setSoknadId(soknadId);

        Faktum soknadInnsendingsDato = new Faktum()
                .medSoknadId(soknadId)
                .medKey("soknadInnsendingsDato")
                .medValue(String.valueOf(innsendtDato.getMillis()))
                .medType(SYSTEMREGISTRERT);
        faktaService.lagreSystemFaktum(soknadId, soknadInnsendingsDato, "");
        soknad.setFakta(repository.hentAlleBrukerData(soknadId));

        soknad.setVedlegg(hentVedleggOgPersister(new XMLMetadataListe(filtrertXmlVedleggListe), soknadId));

        return soknad;
    }

    private static Predicate<XMLMetadata> kvittering() {
        return new Predicate<XMLMetadata>() {
            @Override
            public boolean evaluate(XMLMetadata xmlMetadata) {
                return xmlMetadata instanceof XMLVedlegg && KVITTERING.equals(((XMLVedlegg) xmlMetadata).getSkjemanummer());
            }
        };
    }

    @Override
    public void sendSoknad(String behandlingsId, byte[] pdf) {
        WebSoknad soknad = hentSoknadMedFaktaOgVedlegg(behandlingsId);
        sendSoknad(soknad, pdf);
    }

    private void sendSoknad(WebSoknad soknad, byte[] pdf) {
        long soknadId = soknad.getSoknadId();
        if (soknad.erEttersending() && soknad.getOpplastedeVedlegg().size() <= 0) {
            logger.error("Kan ikke sende inn ettersendingen med ID {0} uten å ha lastet opp vedlegg", soknad.getBrukerBehandlingId());
            throw new ApplicationException("Kan ikke sende inn ettersendingen uten å ha lastet opp vedlegg");
        }

        if (soknad.harAnnetVedleggSomIkkeErLastetOpp()) {
            logger.error("Kan ikke sende inn behandling (ID: {0}) med Annet vedlegg (skjemanummer N6) som ikke er lastet opp", soknad.getBrukerBehandlingId());
            throw new ApplicationException("Kan ikke sende inn behandling uten å ha lastet opp alle  vedlegg med skjemanummer N6");
        }

        logger.info("Lagrer søknad som fil til henvendelse for behandling {}", soknad.getBrukerBehandlingId());
        fillagerService.lagreFil(soknad.getBrukerBehandlingId(), soknad.getUuid(), soknad.getAktoerId(), new ByteArrayInputStream(pdf));

        List<Vedlegg> vedleggForventninger = hentVedleggOgKvittering(soknad);

        String skjemanummer = skjemanummer(soknad);
        XMLHovedskjema hovedskjema = new XMLHovedskjema()
                .withInnsendingsvalg(LASTET_OPP.toString())
                .withSkjemanummer(skjemanummer)
                .withFilnavn(skjemanummer)
                .withMimetype("application/pdf")
                .withFilstorrelse("" + pdf.length)
                .withUuid(soknad.getUuid())
                .withJournalforendeEnhet(journalforendeEnhet(soknad));

        henvendelseService.avsluttSoknad(soknad.getBrukerBehandlingId(), hovedskjema, Transformers.convertToXmlVedleggListe(vedleggForventninger));
        repository.slettSoknad(soknadId);
    }

    private String skjemanummer(WebSoknad soknad) {
        return soknad.erDagpengeSoknad() ? DagpengerUtils.getSkjemanummer(soknad) : soknad.getskjemaNummer();
    }

    private String journalforendeEnhet(WebSoknad soknad) {
        String journalforendeEnhet;

        if (soknad.erDagpengeSoknad()) {
            journalforendeEnhet = DagpengerUtils.getJournalforendeEnhet(soknad);
        } else if (soknad.erAapSoknad() && adresserOgStatsborgerskap(soknad).harUtenlandskFolkeregistrertAdresse()) {
            journalforendeEnhet = AAP_INTERNASJONAL;
        } else {
            journalforendeEnhet = soknad.getJournalforendeEnhet();
        }

        return journalforendeEnhet;
    }

    private List<Vedlegg> hentVedleggOgKvittering(WebSoknad soknad) {
        List<Vedlegg> vedleggForventninger = soknad.getVedlegg();
        Vedlegg kvittering = vedleggRepository.hentVedleggForskjemaNummer(soknad.getSoknadId(), null, KVITTERING);
        if (kvittering != null) {
            vedleggForventninger.add(kvittering);
        }
        return vedleggForventninger;
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
    public void avbrytSoknad(String behandlingsId) {
        WebSoknad soknad = repository.hentSoknad(behandlingsId);

        /**
         * Sletter alle vedlegg til søknader som blir avbrutt.
         * Dette burde egentlig gjøres i henvendelse, siden vi uansett skal slette alle vedlegg på avbrutte søknader.
         * I tillegg blir det liggende igjen mange vedlegg for søknader som er avbrutt før dette kallet ble lagt til.
         * */

        fillagerService.slettAlle(soknad.getBrukerBehandlingId());
        henvendelseService.avbrytSoknad(soknad.getBrukerBehandlingId());
        repository.slettSoknad(soknad.getSoknadId());
    }

    @Override
    public String startSoknad(String navSoknadId, String fodselsnummer) {
        validerSkjemanummer(navSoknadId);
        String mainUid = randomUUID().toString();
        String behandlingsId = henvendelseService
                .startSoknad(getSubjectHandler().getUid(), navSoknadId, mainUid);

        WebSoknad soknad = WebSoknad.startSoknad()
                .medBehandlingId(behandlingsId)
                .medskjemaNummer(navSoknadId)
                .medUuid(mainUid)
                .medAktorId(getSubjectHandler().getUid())
                .medOppretteDato(DateTime.now());

        Long soknadId = repository.opprettSoknad(soknad);
        soknad.setSoknadId(soknadId);

        Faktum bolkerFaktum = new Faktum().medSoknadId(soknadId).medKey("bolker").medType(BRUKERREGISTRERT);
        repository.lagreFaktum(soknadId, bolkerFaktum);

        prepopulerSoknadsFakta(soknadId);
        opprettFaktumForLonnsOgTrekkoppgave(soknadId);
        return behandlingsId;
    }

    private void opprettFaktumForLonnsOgTrekkoppgave(Long soknadId) {
        Faktum lonnsOgTrekkoppgaveFaktum = new Faktum()
                .medSoknadId(soknadId)
                .medKey("lonnsOgTrekkOppgave")
                .medType(SYSTEMREGISTRERT)
                .medValue(startDatoService.erJanuarEllerFebruar().toString());
        faktaService.lagreSystemFaktum(soknadId, lonnsOgTrekkoppgaveFaktum, "");
    }

    private void prepopulerSoknadsFakta(Long soknadId) {
        SoknadStruktur soknadStruktur = hentSoknadStruktur(soknadId);
        List<SoknadFaktum> fakta = soknadStruktur.getFakta();
        Collections.sort(fakta, SoknadFaktum.sammenlignEtterDependOn());

        for (SoknadFaktum soknadFaktum : fakta) {
            if (erIkkeSystemfaktumOgKunEtErTillatt(soknadFaktum)) {
                Faktum f = new Faktum()
                        .medKey(soknadFaktum.getId())
                        .medValue("")
                        .medType(Faktum.FaktumType.BRUKERREGISTRERT);

                if (soknadFaktum.getDependOn() != null) {
                    Faktum parentFaktum = repository.hentFaktumMedKey(soknadId, soknadFaktum.getDependOn().getId());
                    f.setParrentFaktum(parentFaktum.getFaktumId());
                }
                repository.lagreFaktum(soknadId, f);
            }
        }
    }

    private boolean erIkkeSystemfaktumOgKunEtErTillatt(SoknadFaktum faktum) {
        String flereTillatt = faktum.getFlereTillatt();
        String erSystemFaktum = faktum.getErSystemFaktum();
        return !((flereTillatt != null && flereTillatt.equals("true")) || (erSystemFaktum != null && erSystemFaktum.equals("true")));
    }

    private void validerSkjemanummer(String navSoknadId) {
        if (!kravdialogInformasjonHolder.hentAlleSkjemanumre().contains(navSoknadId)) {
            throw new ApplicationException("Ikke gyldig skjemanummer " + navSoknadId);
        }
    }

    @Override
    public SoknadStruktur hentSoknadStruktur(Long soknadId) {
        return config.hentStruktur(soknadId);
    }

    @Override
    public SoknadStruktur hentSoknadStruktur(String skjemanummer) {
        return config.hentStruktur(skjemanummer);
    }

    private void medKodeverk(Vedlegg vedlegg) {
        try {
            Map<Kodeverk.Nokkel, String> koder = kodeverk.getKoder(vedlegg.getSkjemaNummer());
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

    private static final Comparator<WSBehandlingskjedeElement> SORTER_INNSENDT_DATO = new Comparator<WSBehandlingskjedeElement>() {
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
    };

    private void oppdaterKjentInformasjon(String fodselsnummer, WebSoknad soknad) {
        if (soknad.erEttersending()) {
            lagrePersonalia(fodselsnummer, soknad.getSoknadId());
        } else {
            lagreAllInformasjon(fodselsnummer, soknad.getSoknadId());
        }
    }

    private void lagrePersonalia(String fodselsnummer, Long soknadId) {
        bolker.get(PersonaliaService.class.getName()).lagreBolk(fodselsnummer, soknadId);
    }

    private void lagreAllInformasjon(String fodselsnummer, Long soknadId) {
        List<BolkService> soknadBolker = config.getSoknadBolker(soknadId, bolker.values());
        for (BolkService bolk : soknadBolker) {
            bolk.lagreBolk(fodselsnummer, soknadId);
        }
    }
}