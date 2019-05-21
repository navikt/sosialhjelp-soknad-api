package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.DiffFlags;
import com.flipkart.zjsonpatch.JsonDiff;
import no.nav.metrics.MetricsFactory;
import no.nav.metrics.Timer;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
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
import no.nav.sbl.soknadsosialhjelp.json.AdresseMixIn;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.domain.VedleggType;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;
import no.nav.sbl.sosialhjelp.midlertidig.WebSoknadConverter;
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

import static com.flipkart.zjsonpatch.DiffFlags.*;
import static java.util.Collections.sort;
import static java.util.UUID.randomUUID;
import static javax.xml.bind.JAXB.unmarshal;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur.sammenlignEtterDependOn;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.FiksMetadataTransformer.FIKS_ENHET_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.FiksMetadataTransformer.FIKS_ORGNR_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.JsonVedleggUtils.getVedleggFromInternalSoknad;
import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidSoknad;
import static no.nav.sbl.sosialhjelp.domain.Vedleggstatus.Status.LastetOpp;
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
    private WebSoknadConfig webSoknadConfig;

    @Inject
    private NavMessageSource messageSource;

    @Inject
    AlternativRepresentasjonService alternativRepresentasjonService;

    @Inject
    EkstraMetadataService ekstraMetadataService;

    @Inject
    private SoknadMetricsService soknadMetricsService;

    @Inject
    private WebSoknadConverter webSoknadConverter;

    @Inject
    private InnsendingService innsendingService;

    @Inject
    private SoknadUnderArbeidService soknadUnderArbeidService;

    @Inject
    private OpplastetVedleggService opplastetVedleggService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

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
                                .withBeskrivelseAvAnnet(new JsonOkonomibeskrivelserAvAnnet()
                                    .withKilde(JsonKildeBruker.BRUKER)
                                        .withBarneutgifter("")
                                        .withBoutgifter("")
                                        .withSparing("")
                                        .withUtbetaling("")
                                        .withVerdi("")
                                )
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
        final String eier = OidcFeatureToggleUtils.getUserId();
        Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        SoknadUnderArbeid soknadUnderArbeid;
        if (soknadUnderArbeidOptional.isPresent() && soknadUnderArbeidOptional.get().erEttersendelse()){
            soknadUnderArbeid = soknadUnderArbeidOptional.get();
            if (getVedleggFromInternalSoknad(soknadUnderArbeid).isEmpty()){
                logger.error("Kan ikke sende inn ettersendingen med ID {0} uten å ha lastet opp vedlegg", behandlingsId);
                throw new ApplicationException("Kan ikke sende inn ettersendingen uten å ha lastet opp vedlegg");
            }
            logger.info("Starter innsending av søknad med behandlingsId {}", behandlingsId);

            VedleggMetadataListe vedlegg = convertToVedleggMetadataListe(soknadUnderArbeid);
            Map<String, String> ekstraMetadata = hentEkstraMetadata(soknadUnderArbeid);

            HovedskjemaMetadata hovedskjema = lagHovedskjema("");
            henvendelseService.oppdaterMetadataVedAvslutningAvSoknad(behandlingsId, hovedskjema, vedlegg, ekstraMetadata);
            oppgaveHandterer.leggTilOppgave(behandlingsId, eier);

            try {
                WebSoknad soknad = hentSoknad(behandlingsId, MED_DATA, MED_VEDLEGG);
                lokalDb.slettSoknad(soknad, HendelseType.INNSENDT);
            } catch (Exception ignored) { }

            forberedInnsendingMedNyModell(soknadUnderArbeid);

            soknadMetricsService.sendtSoknad("NAV 35-18.01", true);
        } else {
            WebSoknad soknad = hentSoknad(behandlingsId, MED_DATA, MED_VEDLEGG);
            if (soknad.erEttersending() && soknad.getOpplastedeVedlegg().isEmpty()) {
                logger.error("Kan ikke sende inn ettersendingen med ID {0} uten å ha lastet opp vedlegg", behandlingsId);
                throw new ApplicationException("Kan ikke sende inn ettersendingen uten å ha lastet opp vedlegg");
            }

            logger.info("Starter innsending av søknad med behandlingsId {}", behandlingsId);

            legacyKonverterVedleggOgOppdaterSoknadUnderArbeid(behandlingsId, eier, soknad);

            soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();

            HovedskjemaMetadata hovedskjema = lagHovedskjema(soknad.getUuid());
            final VedleggMetadataListe vedlegg = convertToVedleggMetadataListe(soknadUnderArbeid);
            final Map<String, String> ekstraMetadata = hentEkstraMetadata(soknadUnderArbeid);

            henvendelseService.oppdaterMetadataVedAvslutningAvSoknad(behandlingsId, hovedskjema, vedlegg, ekstraMetadata);
            oppgaveHandterer.leggTilOppgave(behandlingsId, eier);
            lokalDb.slettSoknad(soknad, HendelseType.INNSENDT);

            forberedInnsendingMedNyModell(soknadUnderArbeid);

            soknadMetricsService.sendtSoknad(soknad.getskjemaNummer(), soknad.erEttersending());
        }
        if(!soknadUnderArbeid.erEttersendelse()){
            logAlderTilKibana(eier);
        }
    }

    public void legacyKonverterVedleggOgOppdaterSoknadUnderArbeid(String behandlingsId, String eier, WebSoknad soknad) {
        soknad.fjernFaktaSomIkkeSkalVaereSynligISoknaden(webSoknadConfig.hentStruktur(soknad.getskjemaNummer()));
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());

        final SoknadUnderArbeid konvertertSoknadUnderArbeid = webSoknadConverter.mapWebSoknadTilSoknadUnderArbeid(soknad, true);
        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();

        logDersomForskjellMellomFaktumOgNyModell(konvertertSoknadUnderArbeid, soknadUnderArbeid, "DIGISOS-1212 Forskjell i json: ");

        soknadUnderArbeidService.oppdaterEllerOpprettSoknadUnderArbeid(konvertertSoknadUnderArbeid, eier);
        final Long soknadUnderArbeidId = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get().getSoknadId();
        opplastetVedleggService.legacyConvertVedleggToOpplastetVedleggAndUploadToRepositoryAndSetVedleggstatus(behandlingsId, eier, soknadUnderArbeidId);
    }

    public void logDersomForskjellMellomFaktumOgNyModell(SoknadUnderArbeid konvertertSoknadUnderArbeid, SoknadUnderArbeid soknadUnderArbeid, String melding) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(JsonAdresse.class, AdresseMixIn.class);
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        JsonSoknad soknad = soknadUnderArbeid.getJsonInternalSoknad().getSoknad();
        JsonSoknad soknadKonvertert = konvertertSoknadUnderArbeid.getJsonInternalSoknad().getSoknad();
        sortOkonomi(soknad.getData().getOkonomi());
        sortArbeid(soknad.getData().getArbeid());
        sortOkonomi(soknadKonvertert.getData().getOkonomi());
        sortArbeid(soknadKonvertert.getData().getArbeid());
        if (!soknad.equals(soknadKonvertert)){
            try {
                byte[] jsonSoknad = mapJsonSoknadTilFil(soknad, writer);
                byte[] jsonSoknadKonvertert = mapJsonSoknadTilFil(soknadKonvertert, writer);
                JsonNode beforeNode = mapper.readTree(jsonSoknadKonvertert);
                JsonNode afterNode = mapper.readTree(jsonSoknad);
                EnumSet<DiffFlags> flags = EnumSet.of(OMIT_MOVE_OPERATION, OMIT_COPY_OPERATION);
                JsonNode patch = JsonDiff.asJson(beforeNode, afterNode, flags);
                for (JsonNode node : patch) {
                    if (node instanceof ObjectNode) {
                        ObjectNode object = (ObjectNode) node;
                        String path = node.path("path").textValue();
                        if (!path.contains("samvarsgrad")){
                            object.remove("value");
                        }
                    }
                }
                if (patch.isArray()){
                    ArrayNode arrayNode = (ArrayNode) patch;
                    for (int i = 0; i < arrayNode.size(); i++){
                        JsonNode node = arrayNode.get(i);
                        String path = node.path("path").textValue();
                        String op = node.path("op").textValue();
                        if (path.contains("komponenter") && op.contains("add")){
                            arrayNode.remove(i);
                            i--;
                        }
                    }
                }
                String diffs = patch.toString();
                if (!"[]".equals(diffs)){
                    logger.info(melding + diffs);
                }
            } catch (IOException ignored) { }
        }
    }

    private void sortArbeid(JsonArbeid arbeid) {
        arbeid.getForhold().sort(Comparator.comparing(JsonArbeidsforhold::getArbeidsgivernavn));
    }

    public void sortOkonomi(JsonOkonomi okonomi) {
        okonomi.getOpplysninger().getBekreftelse().sort(Comparator.comparing(JsonOkonomibekreftelse::getType));
        okonomi.getOpplysninger().getUtbetaling().sort(Comparator.comparing(JsonOkonomiOpplysningUtbetaling::getType));
        okonomi.getOpplysninger().getUtgift().sort(Comparator.comparing(JsonOkonomiOpplysningUtgift::getType));
        okonomi.getOversikt().getInntekt().sort(Comparator.comparing(JsonOkonomioversiktInntekt::getType));
        okonomi.getOversikt().getUtgift().sort(Comparator.comparing(JsonOkonomioversiktUtgift::getType));
        okonomi.getOversikt().getFormue().sort(Comparator.comparing(JsonOkonomioversiktFormue::getType));
    }

    private byte[] mapJsonSoknadTilFil(JsonSoknad jsonSoknad, ObjectWriter writer) {
        try {
            final String soknad = writer.writeValueAsString(jsonSoknad);
            ensureValidSoknad(soknad);
            return soknad.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            logger.error("Kunne ikke konvertere soknad.json til tekststreng", e);
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> hentEkstraMetadata(SoknadUnderArbeid soknadUnderArbeid) {
        final Map<String, String> ekstraMetadata = new HashMap<>();
        ekstraMetadata.put(FIKS_ORGNR_KEY, soknadUnderArbeid.getJsonInternalSoknad().getMottaker().getOrganisasjonsnummer());
        ekstraMetadata.put(FIKS_ENHET_KEY, soknadUnderArbeid.getJsonInternalSoknad().getMottaker().getNavEnhetsnavn());
        return ekstraMetadata;
    }

    private VedleggMetadataListe convertToVedleggMetadataListe(SoknadUnderArbeid soknadUnderArbeid) {
        final List<JsonVedlegg> jsonVedleggs = getVedleggFromInternalSoknad(soknadUnderArbeid);
        VedleggMetadataListe vedlegg = new VedleggMetadataListe();

        vedlegg.vedleggListe = jsonVedleggs.stream().map(SoknadDataFletter::mapJsonVedleggToVedleggMetadata).collect(Collectors.toList());

        return vedlegg;
    }

    private void forberedInnsendingMedNyModell(SoknadUnderArbeid soknadUnderArbeid) {
        if (soknadUnderArbeid != null) {
            final List<Vedleggstatus> vedleggstatuser = mapSoknadToVedleggstatusListe(soknadUnderArbeid);

            innsendingService.opprettSendtSoknad(soknadUnderArbeid, vedleggstatuser);
        }
    }

    private HovedskjemaMetadata lagHovedskjema(String uuid) {
        HovedskjemaMetadata hovedskjema = new HovedskjemaMetadata();
        hovedskjema.filnavn = "NAV 35-18.01";
        hovedskjema.filUuid = uuid;

        return hovedskjema;
    }

    private List<Vedleggstatus> mapSoknadToVedleggstatusListe(SoknadUnderArbeid soknadUnderArbeid) {
        final List<JsonVedlegg> jsonVedleggs = getVedleggFromInternalSoknad(soknadUnderArbeid);

        if (jsonVedleggs.isEmpty()){
            return new ArrayList<>();
        }

        return jsonVedleggs.stream().filter(jsonVedlegg -> !jsonVedlegg.getStatus().equals(LastetOpp.toString()))
                .map(jsonVedlegg -> new Vedleggstatus()
                        .withVedleggType(new VedleggType(jsonVedlegg.getType() + "|" + jsonVedlegg.getTilleggsinfo()))
                        .withEier(soknadUnderArbeid.getEier())
                        .withStatus(Vedleggstatus.Status.valueOf(jsonVedlegg.getStatus())))
                .collect(Collectors.toList());
    }

    private static SoknadMetadata.VedleggMetadata mapJsonVedleggToVedleggMetadata(JsonVedlegg jsonVedlegg) {
        SoknadMetadata.VedleggMetadata m = new SoknadMetadata.VedleggMetadata();
        m.skjema = jsonVedlegg.getType();
        m.tillegg = jsonVedlegg.getTilleggsinfo();
        m.filnavn = jsonVedlegg.getType();
        m.status = Vedlegg.Status.valueOf(jsonVedlegg.getStatus());
        return m;
    }

    private static void logAlderTilKibana(String eier) {
        if (eier != null && Integer.parseInt(eier.substring(0, 1)) < 4){
            String fodselsdato = eier.substring(0, 6);
            DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                    .appendPattern("ddMM")
                    .appendValueReduced(ChronoField.YEAR_OF_ERA, 2, 2, LocalDate.now().minusYears(100))
                    .toFormatter();
            LocalDate birthDate = LocalDate.parse(fodselsdato, fmt);
            int age = calculateAge(birthDate);
            if ( age > 0 && age < 30 ) {
                logger.info("DIGISOS-1164: UNDER30 - Soknad sent av bruker med alder: " + age);
            } else {
                logger.info("DIGISOS-1164: OVER30 - Soknad sent av bruker med alder:" + age);
            }
        }
    }

    static int calculateAge(LocalDate birthDate) {
        if (birthDate != null) {
            return Period.between(birthDate, LocalDate.now()).getYears();
        } else {
            return 0;
        }
    }
}
