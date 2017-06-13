package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.*;
import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.metrics.Timer;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AlleredeHandtertException;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.StartDatoUtil;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import org.apache.commons.collections15.Transformer;
import org.joda.time.DateTime;
import org.joda.time.base.BaseDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadTilleggsstonader;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.sort;
import static java.util.UUID.randomUUID;
import static javax.xml.bind.JAXB.unmarshal;
import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.LASTET_OPP;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.FERDIG;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.valueOf;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur.sammenlignEtterDependOn;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.convertToXmlVedleggListe;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.*;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SoknadDataFletter {

    private static final Logger logger = getLogger(SoknadDataFletter.class);
    private static final boolean MED_DATA = true;
    private static final boolean MED_VEDLEGG = true;
    private final Predicate<WSBehandlingskjedeElement> STATUS_FERDIG = soknad -> FERDIG.equals(valueOf(soknad.getStatus()));

    @Inject
    public ApplicationContext applicationContext;
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
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    @Inject
    private StartDatoUtil startDatoUtil;

    @Inject
    private NavMessageSource messageSource;

    @Inject
    private SoknadMetricsService soknadMetricsService;

    private Map<String, BolkService> bolker;

    @PostConstruct
    public void initBolker() {
        bolker = applicationContext.getBeansOfType(BolkService.class);
    }


    private WebSoknad hentFraHenvendelse(String behandlingsId, boolean hentFaktumOgVedlegg) {
        WSHentSoknadResponse wsSoknadsdata = henvendelseService.hentSoknad(behandlingsId);

        Optional<XMLMetadata> hovedskjemaOptional = ((XMLMetadataListe) wsSoknadsdata.getAny()).getMetadata().stream()
                .filter(xmlMetadata -> xmlMetadata instanceof XMLHovedskjema)
                .findFirst();

        XMLHovedskjema hovedskjema = (XMLHovedskjema) hovedskjemaOptional.orElseThrow(() -> new ApplicationException("Kunne ikke hente opp søknad"));

        SoknadInnsendingStatus status = valueOf(wsSoknadsdata.getStatus());
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

    @Transactional
    public String startSoknad(String skjemanummer) {
        if (!kravdialogInformasjonHolder.hentAlleSkjemanumre().contains(skjemanummer)) {
            throw new ApplicationException("Ikke gyldig skjemanummer " + skjemanummer);
        }
        String soknadsType = kravdialogInformasjonHolder.hentKonfigurasjon(skjemanummer).getSoknadTypePrefix();
        String mainUid = randomUUID().toString();

        Timer startTimer = createDebugTimer("startTimer", soknadsType, mainUid);

        String aktorId = getSubjectHandler().getUid();
        Timer henvendelseTimer = createDebugTimer("startHenvendelse", soknadsType, mainUid);
        String behandlingsId = henvendelseService.startSoknad(aktorId, skjemanummer, mainUid);
        henvendelseTimer.stop();
        henvendelseTimer.report();


        Timer oprettIDbTimer = createDebugTimer("oprettIDb", soknadsType, mainUid);
        Long soknadId = lagreSoknadILokalDb(skjemanummer, mainUid, aktorId, behandlingsId).getSoknadId();
        faktaService.lagreFaktum(soknadId, bolkerFaktum(soknadId));
        faktaService.lagreSystemFaktum(soknadId, personalia(soknadId));
        oprettIDbTimer.stop();
        oprettIDbTimer.report();

        lagreTommeFaktaFraStrukturTilLokalDb(soknadId, skjemanummer, soknadsType, mainUid);

        soknadMetricsService.startetSoknad(skjemanummer, false);

        startTimer.stop();
        startTimer.report();
        return behandlingsId;
    }

    private Timer createDebugTimer(String name, String soknadsType, String id) {
        Timer timer = MetricsFactory.createTimer("debug.startsoknad." + name);
        timer.addFieldToReport("soknadstype", soknadsType);
        timer.addFieldToReport("randomid", id);
        timer.start();
        return timer;
    }

    private void lagreTommeFaktaFraStrukturTilLokalDb(Long soknadId, String skjemanummer, String soknadsType, String id) {
        Timer strukturTimer = createDebugTimer("lagStruktur", soknadsType, id);
        List<FaktumStruktur> faktaStruktur = config.hentStruktur(skjemanummer).getFakta();
        sort(faktaStruktur, sammenlignEtterDependOn());
        strukturTimer.stop();
        strukturTimer.report();

        Timer lagreTimer = createDebugTimer("lagreTommeFakta", soknadsType, id);

        List<Faktum> fakta = new ArrayList<>();
        List<Long> faktumIder = lokalDb.hentLedigeFaktumIder(faktaStruktur.size());
        Map<String, Long> faktumKeyTilFaktumId = new HashMap<>();
        int idNr = 0;

        for (FaktumStruktur faktumStruktur : faktaStruktur) {
            if (faktumStruktur.ikkeSystemFaktum() && faktumStruktur.ikkeFlereTillatt()) {
                Long faktumId = faktumIder.get(idNr++);

                Faktum faktum = new Faktum()
                        .medFaktumId(faktumId)
                        .medSoknadId(soknadId)
                        .medKey(faktumStruktur.getId())
                        .medValue("")
                        .medType(BRUKERREGISTRERT);

                faktumKeyTilFaktumId.put(faktumStruktur.getId(), faktumId);

                if (faktumStruktur.getDependOn() != null) {
                    Long parentId = faktumKeyTilFaktumId.get(faktumStruktur.getDependOn().getId());
                    faktum.setParrentFaktum(parentId);
                }

                fakta.add(faktum);
            }
        }

        lokalDb.batchOpprettTommeFakta(fakta);

        lagreTimer.stop();
        lagreTimer.report();
    }

    private WebSoknad lagreSoknadILokalDb(String skjemanummer, String uuid, String aktorId, String behandlingsId) {
        WebSoknad nySoknad = WebSoknad.startSoknad()
                .medBehandlingId(behandlingsId)
                .medskjemaNummer(skjemanummer)
                .medUuid(uuid)
                .medAktorId(aktorId)
                .medOppretteDato(DateTime.now());

        Long soknadId = lokalDb.opprettSoknad(nySoknad);
        nySoknad.setSoknadId(soknadId);
        return nySoknad;
    }

    private Faktum bolkerFaktum(Long soknadId) {
        return new Faktum()
                .medSoknadId(soknadId)
                .medKey("bolker")
                .medType(BRUKERREGISTRERT);
    }

    private Faktum personalia(Long soknadId) {
        return new Faktum()
                .medSoknadId(soknadId)
                .medType(SYSTEMREGISTRERT)
                .medKey("personalia");
    }

    public WebSoknad hentSoknad(String behandlingsId, boolean medData, boolean medVedlegg) {
        return this.hentSoknad(behandlingsId, medData, medVedlegg, true);
    }

    public WebSoknad hentSoknad(String behandlingsId, boolean medData, boolean medVedlegg, boolean populerSystemfakta) {
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
            soknad = populerSoknadMedData(populerSystemfakta, soknad);
        }

        return erForbiUtfyllingssteget(soknad) ? sjekkDatoVerdierOgOppdaterDelstegStatus(soknad) : soknad;
    }

    private boolean erForbiUtfyllingssteget(WebSoknad soknad){
        return !(soknad.getDelstegStatus() == DelstegStatus.OPPRETTET ||
                soknad.getDelstegStatus() == DelstegStatus.UTFYLLING);
    }

    public WebSoknad sjekkDatoVerdierOgOppdaterDelstegStatus(WebSoknad soknad) {
        SoknadTilleggsstonader soknadTilleggsstonader = new SoknadTilleggsstonader();

        if (soknadTilleggsstonader.getSkjemanummer().contains(soknad.getskjemaNummer())) {
            List<Faktum> periodeFaktum = soknad.getFaktaMedKey("bostotte.samling")
                    .stream()
                    .filter(faktum -> faktum.hasEgenskap("fom"))
                    .filter(faktum -> faktum.hasEgenskap("tom"))
                    .collect(Collectors.toList());

            for (Faktum datofaktum : periodeFaktum) {
                DateTimeFormatter formaterer = DateTimeFormat.forPattern("yyyy-MM-dd");
                try {
                    formaterer.parseLocalDate(datofaktum.getProperties().get("fom"));
                    formaterer.parseLocalDate(datofaktum.getProperties().get("tom"));
                } catch (IllegalArgumentException e) {
                    soknad.medDelstegStatus(DelstegStatus.UTFYLLING);
                    Event event = MetricsFactory.createEvent("stofo.korruptdato");
                    event.addTagToReport("stofo.korruptdato.behandlingId", soknad.getBrukerBehandlingId());
                    event.report();
                }
            }
        }
        return soknad;
    }

    private WebSoknad populerSoknadMedData(boolean populerSystemfakta, WebSoknad soknad) {
        soknad = lokalDb.hentSoknadMedData(soknad.getSoknadId());
        soknad.medSoknadPrefix(config.getSoknadTypePrefix(soknad.getSoknadId()))
                .medSoknadUrl(config.getSoknadUrl(soknad.getSoknadId()))
                .medStegliste(config.getStegliste(soknad.getSoknadId()))
                .medFortsettSoknadUrl(config.getFortsettSoknadUrl(soknad.getSoknadId()));
        if (populerSystemfakta) {
            if (soknad.erEttersending()) {
                faktaService.lagreSystemFakta(soknad, bolker.get(PersonaliaBolk.class.getName()).genererSystemFakta(getSubjectHandler().getUid(), soknad.getSoknadId()));
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

    public void sendSoknad(String behandlingsId, byte[] pdf, byte[] fullSoknad) {
        WebSoknad soknad = hentSoknad(behandlingsId, MED_DATA, MED_VEDLEGG);
        if (soknad.erEttersending() && soknad.getOpplastedeVedlegg().isEmpty()) {
            logger.error("Kan ikke sende inn ettersendingen med ID {0} uten å ha lastet opp vedlegg", soknad.getBrukerBehandlingId());
            throw new ApplicationException("Kan ikke sende inn ettersendingen uten å ha lastet opp vedlegg");
        }

        if (soknad.harAnnetVedleggSomIkkeErLastetOpp()) {
            logger.error("Kan ikke sende inn behandling (ID: {0}) med Annet vedlegg (skjemanummer N6) som ikke er lastet opp", soknad.getBrukerBehandlingId());
            throw new ApplicationException("Kan ikke sende inn behandling uten å ha lastet opp alle  vedlegg med skjemanummer N6");
        }

        logger.info("Lagrer søknad som fil til henvendelse for behandling {}", soknad.getBrukerBehandlingId());
        fillagerService.lagreFil(soknad.getBrukerBehandlingId(), soknad.getUuid(), soknad.getAktoerId(), new ByteArrayInputStream(pdf));

        XMLHovedskjema hovedskjema = lagXmlHovedskjemaMedAlternativRepresentasjon(pdf, soknad, fullSoknad);
        XMLVedlegg[] vedlegg = convertToXmlVedleggListe(vedleggService.hentVedleggOgKvittering(soknad));
        henvendelseService.avsluttSoknad(soknad.getBrukerBehandlingId(), hovedskjema, vedlegg);
        lokalDb.slettSoknad(soknad.getSoknadId());

        soknadMetricsService.sendtSoknad(soknad.getskjemaNummer(), soknad.erEttersending());
    }

    private XMLHovedskjema lagXmlHovedskjemaMedAlternativRepresentasjon(byte[] pdf, WebSoknad soknad, byte[] fullSoknad) {
        XMLHovedskjema hovedskjema = new XMLHovedskjema()
                .withInnsendingsvalg(LASTET_OPP.toString())
                .withSkjemanummer(skjemanummer(soknad))
                .withFilnavn(skjemanummer(soknad))
                .withMimetype("application/pdf")
                .withFilstorrelse("" + pdf.length)
                .withUuid(soknad.getUuid())
                .withJournalforendeEnhet(journalforendeEnhet(soknad));

        if (!soknad.erEttersending()) {
            XMLAlternativRepresentasjonListe xmlAlternativRepresentasjonListe = new XMLAlternativRepresentasjonListe();
            hovedskjema = hovedskjema.withAlternativRepresentasjonListe(
                    xmlAlternativRepresentasjonListe
                            .withAlternativRepresentasjon(lagListeMedXMLAlternativeRepresentasjoner(soknad)));
            if (fullSoknad != null) {
                XMLAlternativRepresentasjon fullSoknadRepr = new XMLAlternativRepresentasjon()
                        .withUuid(UUID.randomUUID().toString())
                        .withFilnavn(skjemanummer(soknad))
                        .withMimetype("application/pdf-fullversjon")
                        .withFilstorrelse("" + fullSoknad.length);
                fillagerService.lagreFil(soknad.getBrukerBehandlingId(), fullSoknadRepr.getUuid(), soknad.getAktoerId(), new ByteArrayInputStream(fullSoknad));
                xmlAlternativRepresentasjonListe.withAlternativRepresentasjon(fullSoknadRepr);
            }
        }

        return hovedskjema;
    }

    private List<XMLAlternativRepresentasjon> lagListeMedXMLAlternativeRepresentasjoner(WebSoknad soknad) {
        List<XMLAlternativRepresentasjon> alternativRepresentasjonListe = new ArrayList<>();
        List<Transformer<WebSoknad, AlternativRepresentasjon>> transformers = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer()).getTransformers(messageSource);
        soknad.fjernFaktaSomIkkeSkalVaereSynligISoknaden(config.hentStruktur(soknad.getskjemaNummer()));
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
        return alternativRepresentasjonListe;
    }

    public Long hentOpprinneligInnsendtDato(String behandlingsId) {
        return henvendelseService.hentBehandlingskjede(behandlingsId).stream()
                .filter(STATUS_FERDIG)
                .sorted(ELDSTE_FORST)
                .findFirst()
                .map(WSBehandlingskjedeElement::getInnsendtDato)
                .map(BaseDateTime::getMillis)
                .orElseThrow(() -> new ApplicationException(String.format("Kunne ikke hente ut opprinneligInnsendtDato for %s", behandlingsId)));
    }

    public String hentSisteInnsendteBehandlingsId(String behandlingsId) {
        return henvendelseService.hentBehandlingskjede(behandlingsId).stream()
                .filter(STATUS_FERDIG)
                .sorted(NYESTE_FORST)
                .findFirst()
                .get()
                .getBehandlingsId();
    }
}
