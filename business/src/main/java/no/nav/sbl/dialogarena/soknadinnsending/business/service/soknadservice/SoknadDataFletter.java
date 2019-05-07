package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.metrics.MetricsFactory;
import no.nav.metrics.Timer;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.HovedskjemaMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadataListe;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.*;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;
import no.nav.sbl.sosialhjelp.midlertidig.VedleggConverter;
import no.nav.sbl.sosialhjelp.midlertidig.WebSoknadConverter;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Collections.sort;
import static java.util.UUID.randomUUID;
import static javax.xml.bind.JAXB.unmarshal;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur.sammenlignEtterDependOn;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.convertToXmlVedleggListe;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.skjemanummer;
import static no.nav.sbl.sosialhjelp.midlertidig.VedleggsforventningConverter.mapVedleggsforventningerTilVedleggstatusListe;
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
    private OppgaveHandterer oppgaveHandterer;
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

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private OpplastetVedleggRepository opplastetVedleggRepository;

    @Inject
    private VedleggConverter vedleggConverter;

    @Inject
    private WebSoknadConverter webSoknadConverter;

    @Inject
    private InnsendingService innsendingService;
    
    @Inject
    private SoknadUnderArbeidService soknadUnderArbeidService;
    
    @Inject
    private SystemdataUpdater systemdata;

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

        String aktorId = OidcFeatureToggleUtils.getUserId();
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
            faktaService.lagreSystemFaktum(soknadId, arbeidsforhold(soknadId));


        }

        oprettIDbTimer.stop();
        oprettIDbTimer.report();

        lagreTommeFaktaFraStrukturTilLokalDb(soknadId, skjemanummer, soknadnavn, mainUid);

        soknadMetricsService.startetSoknad(skjemanummer, false);

        final SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withVersjon(1L)
                .withEier(aktorId)
                .withBehandlingsId(behandlingsId)
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(aktorId))
                .withInnsendingStatus(SoknadInnsendingStatus.UNDER_ARBEID)
                .withOpprettetDato(LocalDateTime.now())
                .withSistEndretDato(LocalDateTime.now());

        systemdata.update(soknadUnderArbeid);

        soknadUnderArbeidService.oppdaterEllerOpprettSoknadUnderArbeid(soknadUnderArbeid, aktorId);
        
        startTimer.stop();
        startTimer.report();
        
        return behandlingsId;
    }
    
    public static JsonInternalSoknad createEmptyJsonInternalSoknad(String eier) {
        return new JsonInternalSoknad().withSoknad(new JsonSoknad()
                    .withData(new JsonData()
                        .withPersonalia(new JsonPersonalia()
                            .withPersonIdentifikator(new JsonPersonIdentifikator()
                                .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                                .withVerdi(eier)
                            )
                            .withNavn(new JsonSokernavn()
                                .withKilde(JsonSokernavn.Kilde.SYSTEM)
                                .withFornavn("")
                                .withMellomnavn("")
                                .withEtternavn("")
                            )
                            .withKontonummer(new JsonKontonummer()
                                .withKilde(JsonKilde.SYSTEM)
                            )
                        )
                        .withArbeid(new JsonArbeid())
                        .withUtdanning(new JsonUtdanning()
                            .withKilde(JsonKilde.BRUKER)
                        )
                        .withFamilie(new JsonFamilie()
                            .withForsorgerplikt(new JsonForsorgerplikt())
                        )
                        .withBegrunnelse(new JsonBegrunnelse()
                            .withKilde(JsonKildeBruker.BRUKER)
                            .withHvorforSoke("")
                            .withHvaSokesOm("")
                        )
                        .withBosituasjon(new JsonBosituasjon()
                            .withKilde(JsonKildeBruker.BRUKER)
                        )
                        .withOkonomi(new JsonOkonomi()
                            .withOpplysninger(new JsonOkonomiopplysninger()
                                .withUtbetaling(new ArrayList<>())
                                .withUtgift(new ArrayList<>())
                            )
                            .withOversikt(new JsonOkonomioversikt()
                                .withInntekt(new ArrayList<>())
                                .withUtgift(new ArrayList<>())
                                .withFormue(new ArrayList<>())
                            )
                        )
                    )
                    .withDriftsinformasjon("")
                    .withKompatibilitet(new ArrayList<>())
                );
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
                faktaService.lagreSystemFakta(soknad, bolker.get(PersonaliaBolk.class.getName()).genererSystemFakta(OidcFeatureToggleUtils.getUserId(), soknad.getSoknadId()));
            } else {
                List<Faktum> systemfaktum = new ArrayList<>();
                for (BolkService bolk : config.getSoknadBolker(soknad, bolker.values())) {
                    systemfaktum.addAll(bolk.genererSystemFakta(OidcFeatureToggleUtils.getUserId(), soknad.getSoknadId()));
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

    public void sendSoknad(String behandlingsId) {
        WebSoknad soknad = hentSoknad(behandlingsId, MED_DATA, MED_VEDLEGG);
        if (soknad.erEttersending() && soknad.getOpplastedeVedlegg().isEmpty()) {
            logger.error("Kan ikke sende inn ettersendingen med ID {0} uten å ha lastet opp vedlegg", soknad.getBrukerBehandlingId());
            throw new ApplicationException("Kan ikke sende inn ettersendingen uten å ha lastet opp vedlegg");
        }

        logger.info("Starter innsending av søknad med behandlingsId {}", soknad.getBrukerBehandlingId());

        HovedskjemaMetadata hovedskjema = lagHovedskjemaMedAlternativRepresentasjon(soknad);
        final List<Vedlegg> vedleggListe = vedleggService.hentVedleggOgKvittering(soknad);
        VedleggMetadataListe vedlegg = convertToXmlVedleggListe(vedleggListe);
        Map<String, String> ekstraMetadata = ekstraMetadataService.hentEkstraMetadata(soknad);

        final SoknadUnderArbeid konvertertSoknadUnderArbeid = webSoknadConverter.mapWebSoknadTilSoknadUnderArbeid(soknad, true);

        final String eier = OidcFeatureToggleUtils.getUserId();
        soknadUnderArbeidService.oppdaterEllerOpprettSoknadUnderArbeid(konvertertSoknadUnderArbeid, eier);

        final SoknadUnderArbeid soknadUnderArbeid = lagreSoknadOgVedleggMedNyModell(soknad, vedleggListe);

        henvendelseService.oppdaterMetadataVedAvslutningAvSoknad(soknad.getBrukerBehandlingId(), hovedskjema, vedlegg, ekstraMetadata);
        oppgaveHandterer.leggTilOppgave(behandlingsId, soknad.getAktoerId());
        lokalDb.slettSoknad(soknad,HendelseType.INNSENDT);

        forberedInnsendingMedNyModell(soknadUnderArbeid, vedleggListe);

        soknadMetricsService.sendtSoknad(soknad.getskjemaNummer(), soknad.erEttersending());
    }

    private SoknadUnderArbeid lagreSoknadOgVedleggMedNyModell(WebSoknad soknad, List<Vedlegg> vedleggListe) {
        SoknadUnderArbeid soknadUnderArbeid;
        Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknad(soknad.getBrukerBehandlingId(), soknad.getAktoerId());
        if (soknadUnderArbeidOptional.isPresent()) {
            soknadUnderArbeid = soknadUnderArbeidOptional.get();
        } else {
            final SoknadUnderArbeid soknadUnderArbeidFraWebSoknad = webSoknadConverter.mapWebSoknadTilSoknadUnderArbeid(soknad, true);
            if (soknadUnderArbeidFraWebSoknad != null) {
                soknadUnderArbeid = soknadUnderArbeidFraWebSoknad;
                final Long soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeidFraWebSoknad, soknad.getAktoerId());
                soknadUnderArbeid.setSoknadId(soknadUnderArbeidId);
            } else {
                throw new RuntimeException("Kan ikke konvertere fra websøknad under innsending");
            }
        }

        final List<OpplastetVedlegg> opplastedeVedlegg = vedleggConverter.mapVedleggListeTilOpplastetVedleggListe(soknadUnderArbeid.getSoknadId(),
                soknadUnderArbeid.getEier(), vedleggListe);
        if (opplastedeVedlegg != null && !opplastedeVedlegg.isEmpty()) {
            for (OpplastetVedlegg opplastetVedlegg : opplastedeVedlegg) {
                opplastetVedleggRepository.opprettVedlegg(opplastetVedlegg, soknadUnderArbeid.getEier());
            }
        }
        return soknadUnderArbeid;
    }

    private void forberedInnsendingMedNyModell(SoknadUnderArbeid soknadUnderArbeid, List<Vedlegg> vedlegg) {
        if (soknadUnderArbeid != null) {
            List<Vedleggstatus> vedleggstatuser = mapVedleggsforventningerTilVedleggstatusListe(vedlegg, soknadUnderArbeid.getEier());
            innsendingService.opprettSendtSoknad(soknadUnderArbeid, vedleggstatuser);
        }
    }

    private HovedskjemaMetadata lagHovedskjemaMedAlternativRepresentasjon(WebSoknad soknad) {
        HovedskjemaMetadata hovedskjema = new HovedskjemaMetadata();
        hovedskjema.filnavn = skjemanummer(soknad);
        hovedskjema.filUuid = soknad.getUuid();

        List<AlternativRepresentasjon> alternativeRepresentasjoner = alternativRepresentasjonService.hentAlternativeRepresentasjoner(soknad, messageSource);
        alternativRepresentasjonService.lagreTilFillager(soknad.getBrukerBehandlingId(), soknad.getAktoerId(), alternativeRepresentasjoner);
        hovedskjema.alternativRepresentasjon.addAll(alternativRepresentasjonService.lagXmlFormat(alternativeRepresentasjoner));

        return hovedskjema;
    }
}
