package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.metrics.MetricsFactory;
import no.nav.metrics.Timer;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.*;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.util.*;

import static java.util.Collections.sort;
import static java.util.UUID.randomUUID;
import static javax.xml.bind.JAXB.unmarshal;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur.sammenlignEtterDependOn;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.convertToXmlVedleggListe;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.skjemanummer;
import static org.slf4j.LoggerFactory.getLogger;


@Component
public class SoknadDataFletter {

    private static final Logger logger = getLogger(SoknadDataFletter.class);
    private static final boolean MED_DATA = true;
    private static final boolean MED_VEDLEGG = true;

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
    private HendelseRepository hendelseRepository;
    @Inject
    private WebSoknadConfig config;
    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    @Inject
    private NavMessageSource messageSource;

    @Inject
    AlternativRepresentasjonService alternativRepresentasjonService;

    @Inject
    EkstraMetadataService ekstraMetadataService;

    @Inject
    private SoknadMetricsService soknadMetricsService;

    private Map<String, BolkService> bolker;

    @PostConstruct
    public void initBolker() {
        bolker = applicationContext.getBeansOfType(BolkService.class);
    }


    private WebSoknad hentFraHenvendelse(String behandlingsId, boolean hentFaktumOgVedlegg) {
        SoknadMetadata soknadMetadata = henvendelseService.hentSoknad(behandlingsId, true);

        if (UNDER_ARBEID.equals(soknadMetadata.status)) {
            byte[] xmlFraFillager = fillagerService.hentFil(soknadMetadata.hovedskjema.filUuid);
            WebSoknad soknadFraFillager = unmarshal(new ByteArrayInputStream(xmlFraFillager), WebSoknad.class);
            lokalDb.populerFraStruktur(soknadFraFillager);
            vedleggService.populerVedleggMedDataFraHenvendelse(soknadFraFillager, fillagerService.hentFiler(soknadFraFillager.getBrukerBehandlingId()));
            if (hentFaktumOgVedlegg) {
                return lokalDb.hentSoknadMedVedlegg(behandlingsId);
            }
            return lokalDb.hentSoknad(behandlingsId);
        } else {
            // søkndadsdata er slettet, har kun metadata
            return new WebSoknad()
                    .medBehandlingId(behandlingsId)
                    .medStatus(soknadMetadata.status)
                    .medskjemaNummer(soknadMetadata.skjema);
        }
    }

    @Transactional
    public String startSoknad(String skjemanummer) {
        if (!kravdialogInformasjonHolder.hentAlleSkjemanumre().contains(skjemanummer)) {
            throw new ApplicationException("Ikke gyldig skjemanummer " + skjemanummer);
        }
        KravdialogInformasjon kravdialog = kravdialogInformasjonHolder.hentKonfigurasjon(skjemanummer);
        String soknadnavn = kravdialog.getSoknadTypePrefix();
        SoknadType soknadType = kravdialog.getSoknadstype();
        String mainUid = randomUUID().toString();

        Timer startTimer = createDebugTimer("startTimer", soknadnavn, mainUid);

        String aktorId = getSubjectHandler().getUid();
        Timer henvendelseTimer = createDebugTimer("startHenvendelse", soknadnavn, mainUid);
        String behandlingsId = henvendelseService.startSoknad(aktorId, skjemanummer, mainUid, soknadType);
        henvendelseTimer.stop();
        henvendelseTimer.report();


        Timer oprettIDbTimer = createDebugTimer("oprettIDb", soknadnavn, mainUid);
        int versjon = kravdialog.getSkjemaVersjon();
        Long soknadId = lagreSoknadILokalDb(skjemanummer, mainUid, aktorId, behandlingsId, versjon).getSoknadId();
        faktaService.lagreFaktum(soknadId, bolkerFaktum(soknadId));
        faktaService.lagreSystemFaktum(soknadId, personalia(soknadId));

        /*  TODO: Teknisk gjeld -
         *    Bakgrunn for endringen i no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter
         *
         *    Koden som skal håndtere at man ikke overskriver systemdata med brukerdata er basert på at faktum allerede har blitt lagret
         *   (sjekken ligger i "SoknadRepository"). Opprettelse av søknaden og populering av data (bolk) gjøres som to separate steg.
         *   Av sikkerhetsgrunner er det derfor nødvendig å lagre systemfaktumet som en del av søknadsopprettelsen.
         *
         *    Det å opprette systemfaktum som en del av søknadsopprettelse benyttes allerede i dag for å sikre "personalia".
         *
         *    For søknad om økonomisk sosialhjelp er det viktig at arbeidsholddataene ikke kan redigeres av bruker uten at dette blir markert.
         *    Vi trenger derfor å benytte samme løsning for å sikre "arbeidsforhold" som i dag benyttes for "personalia".
         *
         *    Løsninger hvis man ønsker å bli kvitt behovet for å opprette faktum som en del av søknadsopprettelse:
         *
         *    1) Kalle på bolkene og lagre systemdata som en del av søknadsopprettelsen.
         *    2) Bruke XML-definisjonen til faktumet for å verifisere at brukerendring er tillatt fremfor dagens løsning
         *    som er basert på sjekk av lagret faktum.
         */
        if (SosialhjelpInformasjon.SKJEMANUMMER.equals(skjemanummer)) {
            //faktaService.lagreSystemFaktum(soknadId, arbeidsforhold(soknadId));


        }

        oprettIDbTimer.stop();
        oprettIDbTimer.report();

        lagreTommeFaktaFraStrukturTilLokalDb(soknadId, skjemanummer, soknadnavn, mainUid);

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

    private WebSoknad lagreSoknadILokalDb(String skjemanummer, String uuid, String aktorId, String behandlingsId, int versjon) {
        WebSoknad nySoknad = WebSoknad.startSoknad()
                .medBehandlingId(behandlingsId)
                .medskjemaNummer(skjemanummer)
                .medUuid(uuid)
                .medAktorId(aktorId)
                .medOppretteDato(DateTime.now())
                .medVersjon(versjon);

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

    private Faktum arbeidsforhold(Long soknadId) {
        return new Faktum()
                .medSoknadId(soknadId)
                .medType(SYSTEMREGISTRERT)
                .medKey("arbeidsforhold");
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

        return soknad;
    }

    private WebSoknad populerSoknadMedData(boolean populerSystemfakta, WebSoknad soknad) {
        soknad = lokalDb.hentSoknadMedData(soknad.getSoknadId());
        soknad.medSoknadPrefix(config.getSoknadTypePrefix(soknad.getSoknadId()))
                .medSoknadUrl(config.getSoknadUrl(soknad.getSoknadId()))
                .medStegliste(config.getStegliste(soknad.getSoknadId()))
                .medVersjon(hendelseRepository.hentVersjon(soknad.getBrukerBehandlingId()))
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

        soknad = lokalDb.hentSoknadMedData(soknad.getSoknadId());
        soknad.medSoknadPrefix(config.getSoknadTypePrefix(soknad.getSoknadId()))
                .medSoknadUrl(config.getSoknadUrl(soknad.getSoknadId()))
                .medStegliste(config.getStegliste(soknad.getSoknadId()))
                .medFortsettSoknadUrl(config.getFortsettSoknadUrl(soknad.getSoknadId()));
        return soknad;
    }

    public void sendSoknad(String behandlingsId, byte[] pdf, byte[] fullSoknad) {
        WebSoknad soknad = hentSoknad(behandlingsId, MED_DATA, MED_VEDLEGG);
        if (soknad.erEttersending() && soknad.getOpplastedeVedlegg().isEmpty()) {
            logger.error("Kan ikke sende inn ettersendingen med ID {0} uten å ha lastet opp vedlegg", soknad.getBrukerBehandlingId());
            throw new ApplicationException("Kan ikke sende inn ettersendingen uten å ha lastet opp vedlegg");
        }

        logger.info("Lagrer søknad som fil til henvendelse for behandling {}", soknad.getBrukerBehandlingId());
        fillagerService.lagreFil(soknad.getBrukerBehandlingId(), soknad.getUuid(), soknad.getAktoerId(), new ByteArrayInputStream(pdf));

        HovedskjemaMetadata hovedskjema = lagHovedskjemaMedAlternativRepresentasjon(pdf, soknad, fullSoknad);
        VedleggMetadataListe vedlegg = convertToXmlVedleggListe(vedleggService.hentVedleggOgKvittering(soknad));
        Map<String, String> ekstraMetadata = ekstraMetadataService.hentEkstraMetadata(soknad);

        henvendelseService.avsluttSoknad(soknad.getBrukerBehandlingId(), hovedskjema, vedlegg, ekstraMetadata);
        lokalDb.slettSoknad(soknad,HendelseType.INNSENDT);

        soknadMetricsService.sendtSoknad(soknad.getskjemaNummer(), soknad.erEttersending());

    }

    private HovedskjemaMetadata lagHovedskjemaMedAlternativRepresentasjon(byte[] pdf, WebSoknad soknad, byte[] fullSoknad) {
        HovedskjemaMetadata hovedskjema = new HovedskjemaMetadata();
        hovedskjema.filnavn = skjemanummer(soknad);
        hovedskjema.mimetype = "application/pdf";
        hovedskjema.filStorrelse = "" + pdf.length;
        hovedskjema.filUuid = soknad.getUuid();

        List<AlternativRepresentasjon> alternativeRepresentasjoner = alternativRepresentasjonService.hentAlternativeRepresentasjoner(soknad, messageSource);
        alternativRepresentasjonService.lagreTilFillager(soknad.getBrukerBehandlingId(), soknad.getAktoerId(), alternativeRepresentasjoner);
        hovedskjema.alternativRepresentasjon.addAll(alternativRepresentasjonService.lagXmlFormat(alternativeRepresentasjoner));

        if (fullSoknad != null) {
            FilData full = new FilData();
            full.filUuid = UUID.randomUUID().toString();
            full.filnavn = skjemanummer(soknad);
            full.mimetype = "application/pdf-fullversjon";
            full.filStorrelse ="" + fullSoknad.length;

            fillagerService.lagreFil(soknad.getBrukerBehandlingId(), full.filUuid, soknad.getAktoerId(), new ByteArrayInputStream(fullSoknad));
            hovedskjema.alternativRepresentasjon.add(full);
        }

        return hovedskjema;
    }

    public Long hentOpprinneligInnsendtDato(String behandlingsId) {
        return null; /* henvendelseService.hentBehandlingskjede(behandlingsId).stream()
                .filter(STATUS_FERDIG)
                .sorted(ELDSTE_FORST)
                .findFirst()
                .map(WSBehandlingskjedeElement::getInnsendtDato)
                .map(BaseDateTime::getMillis)
                .orElseThrow(() -> new ApplicationException(String.format("Kunne ikke hente ut opprinneligInnsendtDato for %s", behandlingsId)));*/
    }

    public String hentSisteInnsendteBehandlingsId(String behandlingsId) {
        return null; /*henvendelseService.hentBehandlingskjede(behandlingsId).stream()
                .filter(STATUS_FERDIG)
                .sorted(NYESTE_FORST)
                .findFirst()
                .get()
                .getBehandlingsId();*/
    }
}
