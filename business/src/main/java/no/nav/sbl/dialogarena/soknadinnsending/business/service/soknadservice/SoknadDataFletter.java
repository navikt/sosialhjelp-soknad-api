package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.lang.collections.predicate.InstanceOf;
import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadFaktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.StartDatoService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.sort;
import static java.util.UUID.randomUUID;
import static javax.xml.bind.JAXB.unmarshal;
import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.LASTET_OPP;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.*;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.*;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadFaktum.sammenlignEtterDependOn;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.convertToXmlVedleggListe;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.*;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SoknadDataFletter {

    private static final Logger logger = getLogger(SoknadDataFletter.class);

    @Inject
    private HenvendelseService henvendelseService;

    @Inject
    private FillagerService fillagerService;

    @Inject
    private VedleggService vedleggService;

    @Inject
    private FaktaService faktaService;

    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository lokalDb;

    @Inject
    private WebSoknadConfig config;

    @Inject
    public ApplicationContext applicationContext;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    @Inject
    private StartDatoService startDatoService;

    private Map<String, BolkService> bolker;

    @PostConstruct
    public void initBolker() {
        bolker = applicationContext.getBeansOfType(BolkService.class);
    }

    public SoknadStruktur hentSoknadStruktur(Long soknadId) {
        return config.hentStruktur(soknadId);
    }

    public WebSoknad hentFraHenvendelse(String behandlingsId, boolean hentFaktumOgVedlegg) {
        WSHentSoknadResponse wsSoknadsdata = henvendelseService.hentSoknad(behandlingsId);

        Optional<XMLMetadata> hovedskjemaOptional = on(((XMLMetadataListe) wsSoknadsdata.getAny()).getMetadata())
                .filter(new InstanceOf<XMLMetadata>(XMLHovedskjema.class)).head();
        XMLHovedskjema hovedskjema = (XMLHovedskjema) hovedskjemaOptional.getOrThrow(new ApplicationException("Kunne ikke hente opp søknad"));

        SoknadInnsendingStatus status = SoknadInnsendingStatus.valueOf(wsSoknadsdata.getStatus());
        if (status.equals(UNDER_ARBEID)) {
            WebSoknad soknadFraFillager = unmarshal(new ByteArrayInputStream(fillagerService.hentFil(hovedskjema.getUuid())), WebSoknad.class);
            lokalDb.populerFraStruktur(soknadFraFillager);
            vedleggService.populerVedleggMedDataFraHenvendelse(soknadFraFillager, fillagerService.hentFiler(soknadFraFillager.getBrukerBehandlingId()));
            if (hentFaktumOgVedlegg) {
                return lokalDb.hentSoknadMedVedlegg(behandlingsId);
            }
            return lokalDb.hentSoknad(behandlingsId);
        } else {
            // søkndadsdata er slettet i henvendelse, har kun metadata
            return new WebSoknad()
                    .medBehandlingId(behandlingsId)
                    .medStatus(status)
                    .medskjemaNummer(hovedskjema.getSkjemanummer());
        }
    }

    public String startEttersending(String behandlingsIdSoknad) {
        List<WSBehandlingskjedeElement> behandlingskjede = henvendelseService.hentBehandlingskjede(behandlingsIdSoknad);

        List<WSBehandlingskjedeElement> sorterteBehandlinger = on(behandlingskjede)
                .filter(where(STATUS, not(equalTo(AVBRUTT_AV_BRUKER))))
                .collect(NYESTE_FORST);
        WSHentSoknadResponse wsSoknadsdata = henvendelseService.hentSoknad(sorterteBehandlinger.get(0).getBehandlingsId());

        if (wsSoknadsdata.getInnsendtDato() == null) {
            throw new ApplicationException("Kan ikke starte ettersending på en ikke fullfort soknad");
        }
        String ettersendingsBehandlingId = henvendelseService.startEttersending(wsSoknadsdata);

        String behandlingskjedeId = wsSoknadsdata.getBehandlingsId();
        if (wsSoknadsdata.getBehandlingskjedeId() != null) {
            behandlingskjedeId = wsSoknadsdata.getBehandlingskjedeId();
        }

        WebSoknad ettersending = WebSoknad.startEttersending(ettersendingsBehandlingId);
        List<XMLMetadata> xmlVedleggListe = ((XMLMetadataListe) henvendelseService.hentSoknad(ettersendingsBehandlingId).getAny()).getMetadata();
        List<XMLMetadata> filtrertXmlVedleggListe = on(xmlVedleggListe).filter(not(kvittering())).collect();

        Optional<XMLMetadata> hovedskjema = on(filtrertXmlVedleggListe)
                .filter(new InstanceOf<XMLMetadata>(XMLHovedskjema.class)).head();
        if (!hovedskjema.isSome()) {
            throw new ApplicationException("Kunne ikke hente opp hovedskjema for søknad");
        }
        XMLHovedskjema xmlHovedskjema = (XMLHovedskjema) hovedskjema.get();

        ettersending.medUuid(randomUUID().toString())
                .medAktorId(getSubjectHandler().getUid())
                .medskjemaNummer(xmlHovedskjema.getSkjemanummer())
                .medBehandlingskjedeId(behandlingskjedeId)
                .medJournalforendeEnhet(xmlHovedskjema.getJournalforendeEnhet());

        Long soknadId = lokalDb.opprettSoknad(ettersending);
        ettersending.setSoknadId(soknadId);

        Faktum soknadInnsendingsDato = new Faktum()
                .medSoknadId(soknadId)
                .medKey("soknadInnsendingsDato")
                .medValue(String.valueOf(hentOrginalInnsendtDato(behandlingskjede, behandlingsIdSoknad).getMillis()))
                .medType(SYSTEMREGISTRERT);
        faktaService.lagreSystemFaktum(soknadId, soknadInnsendingsDato);
        ettersending.setFakta(lokalDb.hentAlleBrukerData(soknadId));
        ettersending.setVedlegg(vedleggService.hentVedleggOgPersister(new XMLMetadataListe(filtrertXmlVedleggListe), soknadId));

        return ettersending.getBrukerBehandlingId();
    }

    public Map<String, String> hentInnsendtDatoOgSisteInnsending(String behandlingsId) {
        Map<String, String> result = new HashMap<>();
        List<WSBehandlingskjedeElement> wsBehandlingskjedeElements = henvendelseService.hentBehandlingskjede(behandlingsId);
        List<WSBehandlingskjedeElement> sorterteBehandlinger =
                on(wsBehandlingskjedeElements).filter(where(STATUS, (equalTo(FERDIG)))).collect(SORTER_INNSENDT_DATO);

        WSBehandlingskjedeElement innsendtSoknad = sorterteBehandlinger.get(0);
        result.put("innsendtdatoSoknad", String.valueOf(innsendtSoknad.getInnsendtDato().getMillis()));
        result.put("sistInnsendteBehandlingsId", sorterteBehandlinger.get(sorterteBehandlinger.size() - 1).getBehandlingsId());
        return result;
    }

    @Transactional
    public String startSoknad(String skjemanummer) {
        if (!kravdialogInformasjonHolder.hentAlleSkjemanumre().contains(skjemanummer)) {
            throw new ApplicationException("Ikke gyldig skjemanummer " + skjemanummer);
        }
        String mainUid = randomUUID().toString();
        String aktorId = getSubjectHandler().getUid();
        String behandlingsId = henvendelseService.startSoknad(aktorId, skjemanummer, mainUid);

        WebSoknad nySoknad = WebSoknad.startSoknad()
                .medBehandlingId(behandlingsId)
                .medskjemaNummer(skjemanummer)
                .medUuid(mainUid)
                .medAktorId(aktorId)
                .medOppretteDato(DateTime.now());

        Long soknadId = lokalDb.opprettSoknad(nySoknad);
        nySoknad.setSoknadId(soknadId);
        lokalDb.lagreFaktum(soknadId, new Faktum().medSoknadId(soknadId).medKey("bolker").medType(BRUKERREGISTRERT));

        Faktum personalia = new Faktum()
                .medSoknadId(soknadId)
                .medType(SYSTEMREGISTRERT)
                .medKey("personalia");
        faktaService.lagreSystemFaktum(soknadId, personalia);

        List<SoknadFaktum> fakta = hentSoknadStruktur(soknadId).getFakta();
        sort(fakta, sammenlignEtterDependOn());

        for (SoknadFaktum soknadFaktum : fakta) {
            if (erIkkeSystemfaktumOgKunEtErTillatt(soknadFaktum)) {
                Faktum f = new Faktum()
                        .medKey(soknadFaktum.getId())
                        .medValue("")
                        .medType(BRUKERREGISTRERT);

                if (soknadFaktum.getDependOn() != null) {
                    Faktum parentFaktum = lokalDb.hentFaktumMedKey(soknadId, soknadFaktum.getDependOn().getId());
                    f.setParrentFaktum(parentFaktum.getFaktumId());
                }
                lokalDb.lagreFaktum(soknadId, f);
            }
        }
        Faktum lonnsOgTrekkoppgaveFaktum = new Faktum()
                .medSoknadId(soknadId)
                .medKey("lonnsOgTrekkOppgave")
                .medType(SYSTEMREGISTRERT)
                .medValue(startDatoService.erJanuarEllerFebruar().toString());
        faktaService.lagreSystemFaktum(soknadId, lonnsOgTrekkoppgaveFaktum);

        return behandlingsId;
    }

    public WebSoknad hentSoknad(String behandlingsId, boolean medData, boolean medVedlegg) {
        WebSoknad soknadFraLokalDb;

        if (medVedlegg) {
            soknadFraLokalDb = lokalDb.hentSoknadMedVedlegg(behandlingsId);
        } else {
            soknadFraLokalDb = lokalDb.hentSoknad(behandlingsId);
        }

        WebSoknad soknad;
        if (medData) {
            soknad = soknadFraLokalDb != null ? lokalDb.hentSoknadMedData(soknadFraLokalDb.getSoknadId()) : hentFraHenvendelse(behandlingsId, true);
        } else {
            soknad = soknadFraLokalDb != null ? soknadFraLokalDb : hentFraHenvendelse(behandlingsId, false);
        }

        if (medData) {
            soknad = lokalDb.hentSoknadMedData(soknad.getSoknadId());
            soknad.medSoknadPrefix(config.getSoknadTypePrefix(soknad.getSoknadId()))
                    .medSoknadUrl(config.getSoknadUrl(soknad.getSoknadId()))
                    .medFortsettSoknadUrl(config.getFortsettSoknadUrl(soknad.getSoknadId()));
            if (soknad.erEttersending()) {
                faktaService.lagreSystemFakta(soknad, bolker.get(PersonaliaService.class.getName()).genererSystemFakta(getSubjectHandler().getUid(), soknad.getSoknadId()));
            } else {
                List<Faktum> systemfaktum = new ArrayList<>();
                for (BolkService bolk : config.getSoknadBolker(soknad, bolker.values())) {
                    systemfaktum.addAll(bolk.genererSystemFakta(getSubjectHandler().getUid(), soknad.getSoknadId()));
                }
                faktaService.lagreSystemFakta(soknad, systemfaktum);
            }
        }
        return soknad;
    }

    public void sendSoknad(String behandlingsId, byte[] pdf) {
        WebSoknad soknad = hentSoknad(behandlingsId, true, true);
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

        XMLHovedskjema hovedskjema = new XMLHovedskjema()
                .withInnsendingsvalg(LASTET_OPP.toString())
                .withSkjemanummer(skjemanummer(soknad))
                .withFilnavn(skjemanummer(soknad))
                .withMimetype("application/pdf")
                .withFilstorrelse("" + pdf.length)
                .withUuid(soknad.getUuid())
                .withJournalforendeEnhet(journalforendeEnhet(soknad));

        /*List<Transformer<WebSoknad, AlternativRepresentasjon>> transformers = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer()).getTransformers();
        XMLAlternativRepresentasjonListe xmlAlternativRepresentasjonListe = new XMLAlternativRepresentasjonListe();

        List<XMLAlternativRepresentasjon> alternativRepresentasjonListe = xmlAlternativRepresentasjonListe.getAlternativRepresentasjon();

        for (Transformer<WebSoknad, AlternativRepresentasjon> transformer : transformers) {
            AlternativRepresentasjon altrep = transformer.transform(soknad);
            fillagerService.lagreFil(soknad.getBrukerBehandlingId(),
                    altrep.getUuid(),
                    soknad.getAktoerId(),
                    new ByteArrayInputStream(altrep.getContent()));

            alternativRepresentasjonListe.add(new XMLAlternativRepresentasjon()
                    .withFilnavn(altrep.getFilnavn())
                    .withFilstorrelse(altrep.getContent().length + "")
                    .withMimetype(altrep.getMimetype())
                    .withUuid(altrep.getUuid()));
        }
        hovedskjema.withAlternativRepresentasjonListe(xmlAlternativRepresentasjonListe);*/

        henvendelseService.avsluttSoknad(soknad.getBrukerBehandlingId(), hovedskjema, convertToXmlVedleggListe(vedleggService.hentVedleggOgKvittering(soknad)));
        lokalDb.slettSoknad(soknad.getSoknadId());
    }

}
