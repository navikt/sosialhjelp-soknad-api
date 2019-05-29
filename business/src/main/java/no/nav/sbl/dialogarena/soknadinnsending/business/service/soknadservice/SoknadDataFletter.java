package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.metrics.MetricsFactory;
import no.nav.metrics.Timer;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadataListe;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
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
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;
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
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static javax.xml.bind.JAXB.unmarshal;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.JsonVedleggUtils.getVedleggFromInternalSoknad;
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
    private FaktaService faktaService;
    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository lokalDb;
    @Inject
    private HendelseRepository hendelseRepository;
    @Inject
    private OppgaveHandterer oppgaveHandterer;
    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    @Inject
    private NavMessageSource messageSource;

    @Inject
    private SoknadMetricsService soknadMetricsService;

    @Inject
    private InnsendingService innsendingService;

    @Inject
    private SoknadUnderArbeidService soknadUnderArbeidService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private SystemdataUpdater systemdata;


    private WebSoknad hentFraHenvendelse(String behandlingsId, boolean hentFaktumOgVedlegg) {
        SoknadMetadata soknadMetadata = henvendelseService.hentSoknad(behandlingsId, true);

        if (UNDER_ARBEID.equals(soknadMetadata.status)) {
            byte[] xmlFraFillager = fillagerService.hentFil(soknadMetadata.hovedskjema.filUuid);
            WebSoknad soknadFraFillager = unmarshal(new ByteArrayInputStream(xmlFraFillager), WebSoknad.class);
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
        String soknadnavn = "soknadsosialhjelp";
        SoknadType soknadType = SoknadType.SEND_SOKNAD_KOMMUNAL;
        String mainUid = randomUUID().toString();

        Timer startTimer = createDebugTimer("startTimer", soknadnavn, mainUid);

        String aktorId = OidcFeatureToggleUtils.getUserId();
        Timer henvendelseTimer = createDebugTimer("startHenvendelse", soknadnavn, mainUid);
        String behandlingsId = henvendelseService.startSoknad(aktorId, skjemanummer, mainUid, soknadType);
        henvendelseTimer.stop();
        henvendelseTimer.report();


        Timer oprettIDbTimer = createDebugTimer("oprettIDb", soknadnavn, mainUid);
        lagreSoknadILokalDb(skjemanummer, mainUid, aktorId, behandlingsId, 0);

        oprettIDbTimer.stop();
        oprettIDbTimer.report();

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

        Iterator<Long> faktumIder = lokalDb.hentLedigeFaktumIder(1).iterator();
        Faktum faktum = new Faktum()
                .medFaktumId(faktumIder.next())
                .medKey("progresjon")
                .medValue("1")
                .medType(BRUKERREGISTRERT)
                .medSoknadId(soknadId);
        faktaService.opprettBrukerFaktum(behandlingsId, faktum);
        List<Faktum> fakta = new ArrayList<>();
        fakta.add(faktum);
        nySoknad.setFakta(fakta);

        return nySoknad;
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
            soknad = lokalDb.hentSoknadMedData(soknad.getSoknadId()).medVersjon(hendelseRepository.hentVersjon(soknad.getBrukerBehandlingId()));
        }

        return soknad;
    }

    public void sendSoknad(String behandlingsId) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        if (soknadUnderArbeid.erEttersendelse() && getVedleggFromInternalSoknad(soknadUnderArbeid).isEmpty()){
            logger.error("Kan ikke sende inn ettersendingen med ID {0} uten å ha lastet opp vedlegg", behandlingsId);
            throw new ApplicationException("Kan ikke sende inn ettersendingen uten å ha lastet opp vedlegg");
        }
        logger.info("Starter innsending av søknad med behandlingsId {}", behandlingsId);

        String webSoknadUuid = "";
        if (!soknadUnderArbeid.erEttersendelse()) {
            webSoknadUuid = hentSoknad(behandlingsId, MED_DATA, MED_VEDLEGG).getUuid();
        }

        VedleggMetadataListe vedlegg = convertToVedleggMetadataListe(soknadUnderArbeid);
        henvendelseService.oppdaterMetadataVedAvslutningAvSoknad(behandlingsId, webSoknadUuid, vedlegg, soknadUnderArbeid);
        oppgaveHandterer.leggTilOppgave(behandlingsId, eier);

        try {
            WebSoknad soknad = hentSoknad(behandlingsId, MED_DATA, MED_VEDLEGG);
            lokalDb.slettSoknad(soknad, HendelseType.INNSENDT);
        } catch (Exception ignored) { }

        forberedInnsendingMedNyModell(soknadUnderArbeid);

        soknadMetricsService.sendtSoknad("NAV 35-18.01", soknadUnderArbeid.erEttersendelse());
        if (!soknadUnderArbeid.erEttersendelse()) {
            logAlderTilKibana(eier);
        }
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
