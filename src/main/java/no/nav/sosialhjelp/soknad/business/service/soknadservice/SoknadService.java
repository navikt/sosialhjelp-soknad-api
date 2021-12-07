package no.nav.sosialhjelp.soknad.business.service.soknadservice;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sosialhjelp.metrics.MetricsFactory;
import no.nav.sosialhjelp.metrics.Timer;
import no.nav.sosialhjelp.soknad.business.InnsendingService;
import no.nav.sosialhjelp.soknad.business.batch.oppgave.OppgaveHandterer;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.business.service.HenvendelseService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus;
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus;
import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.inntekt.husbanken.BostotteSystemdata;
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkatteetatenSystemdata;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static no.nav.sosialhjelp.soknad.business.util.JsonVedleggUtils.getVedleggFromInternalSoknad;
import static no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SosialhjelpInformasjon.SOKNAD_TYPE_PREFIX;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SoknadService {

    private static final Logger logger = getLogger(SoknadService.class);

    private final HenvendelseService henvendelseService;
    private final OppgaveHandterer oppgaveHandterer;
    private final SoknadMetricsService soknadMetricsService;
    private final InnsendingService innsendingService;
    private final EttersendingService ettersendingService;
    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    private final SystemdataUpdater systemdataUpdater;
    private final BostotteSystemdata bostotteSystemdata;
    private final SkatteetatenSystemdata skatteetatenSystemdata;

    public SoknadService(
            HenvendelseService henvendelseService,
            OppgaveHandterer oppgaveHandterer,
            SoknadMetricsService soknadMetricsService,
            InnsendingService innsendingService,
            EttersendingService ettersendingService,
            SoknadUnderArbeidRepository soknadUnderArbeidRepository,
            SystemdataUpdater systemdataUpdater,
            BostotteSystemdata bostotteSystemdata,
            SkatteetatenSystemdata skatteetatenSystemdata
    ) {
        this.henvendelseService = henvendelseService;
        this.oppgaveHandterer = oppgaveHandterer;
        this.soknadMetricsService = soknadMetricsService;
        this.innsendingService = innsendingService;
        this.ettersendingService = ettersendingService;
        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
        this.systemdataUpdater = systemdataUpdater;
        this.bostotteSystemdata = bostotteSystemdata;
        this.skatteetatenSystemdata = skatteetatenSystemdata;
    }

    @Transactional
    public String startSoknad(String token) {
        String mainUid = randomUUID().toString();

        Timer startTimer = createDebugTimer("startTimer", mainUid);

        String aktorId = SubjectHandler.getUserId();
        Timer henvendelseTimer = createDebugTimer("startHenvendelse", mainUid);
        String behandlingsId = henvendelseService.startSoknad(aktorId);
        henvendelseTimer.stop();
        henvendelseTimer.report();

        soknadMetricsService.reportStartSoknad(false);

        Timer oprettIDbTimer = createDebugTimer("oprettIDb", mainUid);

        final SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withVersjon(1L)
                .withEier(aktorId)
                .withBehandlingsId(behandlingsId)
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(aktorId))
                .withStatus(SoknadUnderArbeidStatus.UNDER_ARBEID)
                .withOpprettetDato(LocalDateTime.now())
                .withSistEndretDato(LocalDateTime.now());

        systemdataUpdater.update(soknadUnderArbeid);

        soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, aktorId);

        oprettIDbTimer.stop();
        oprettIDbTimer.report();
        startTimer.stop();
        startTimer.report();

        return behandlingsId;
    }

    @Transactional
    public void sendSoknad(String behandlingsId) {
        final String eier = SubjectHandler.getUserId();
        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);

        logger.info("Starter innsending av søknad med behandlingsId {}", behandlingsId);
        logDriftsinformasjon(soknadUnderArbeid);

        validateEttersendelseHasVedlegg(soknadUnderArbeid);
        SoknadMetadata.VedleggMetadataListe vedlegg = convertToVedleggMetadataListe(soknadUnderArbeid);

        henvendelseService.oppdaterMetadataVedAvslutningAvSoknad(behandlingsId, vedlegg, soknadUnderArbeid, false);
        oppgaveHandterer.leggTilOppgave(behandlingsId, eier);
        innsendingService.opprettSendtSoknad(soknadUnderArbeid);

        soknadMetricsService.reportSendSoknadMetrics(eier, soknadUnderArbeid, vedlegg.vedleggListe);
    }

    private void validateEttersendelseHasVedlegg(SoknadUnderArbeid soknadUnderArbeid) {
        if (soknadUnderArbeid.erEttersendelse() && getVedleggFromInternalSoknad(soknadUnderArbeid).isEmpty()) {
            logger.error("Kan ikke sende inn ettersendingen med ID {} uten å ha lastet opp vedlegg", soknadUnderArbeid.getBehandlingsId());
            throw new SosialhjelpSoknadApiException("Kan ikke sende inn ettersendingen uten å ha lastet opp vedlegg");
        }
    }

    private void logDriftsinformasjon(SoknadUnderArbeid soknadUnderArbeid){
        if(!soknadUnderArbeid.erEttersendelse()) {
            if (Boolean.TRUE.equals(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet())) {
                var alderPaaData = finnAlderPaaDataFor(soknadUnderArbeid, BOSTOTTE_SAMTYKKE);
                logger.info("Nedlasting fra Husbanken har feilet for innsendtsoknad. {}", alderPaaData);
            }
            if (Boolean.TRUE.equals(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getInntektFraSkatteetatenFeilet())) {
                var alderPaaData = finnAlderPaaDataFor(soknadUnderArbeid, UTBETALING_SKATTEETATEN_SAMTYKKE);
                logger.info("Nedlasting fra Skatteetaten har feilet for innsendtsoknad. {}", alderPaaData);
            }
        }
    }

    private String finnAlderPaaDataFor(SoknadUnderArbeid soknadUnderArbeid, String type) {
        String bekreftelsesDatoStreng = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals(type))
                .filter(JsonOkonomibekreftelse::getVerdi)
                .findAny()
                .map(JsonOkonomibekreftelse::getBekreftelsesDato).orElse(null);
        if (bekreftelsesDatoStreng == null) {
            return "";
        }
        OffsetDateTime bekreftelsesDato = OffsetDateTime.parse(bekreftelsesDatoStreng);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return " Dataene er " + bekreftelsesDato.until(now, ChronoUnit.DAYS) + " dager, " +
                bekreftelsesDato.until(now, ChronoUnit.HOURS) % 24 + " timer og " +
                bekreftelsesDato.until(now, ChronoUnit.MINUTES) % 60 + " minutter gamle.";
    }

    @Transactional
    public void avbrytSoknad(String behandlingsId) {
        String eier = SubjectHandler.getUserId();
        Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknadOptional(behandlingsId, eier);
        if (soknadUnderArbeidOptional.isPresent()) {
            soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeidOptional.get(), eier);
            henvendelseService.avbrytSoknad(soknadUnderArbeidOptional.get().getBehandlingsId(), false);
            soknadMetricsService.reportAvbruttSoknad(soknadUnderArbeidOptional.get().erEttersendelse());
        }
    }

    @Transactional
    public void oppdaterSamtykker(String behandlingsId, boolean harBostotteSamtykke, boolean harSkatteetatenSamtykke, String token) {
        final String eier = SubjectHandler.getUserId();
        final SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        if (harSkatteetatenSamtykke) {
            skatteetatenSystemdata.updateSystemdataIn(soknadUnderArbeid);
        }
        if (harBostotteSamtykke) {
            bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, token);
        }
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier);
    }

    public String startEttersending(String behandlingsIdSoknad) {
        return ettersendingService.start(behandlingsIdSoknad);
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
                                        .withBostotte(new JsonBostotte())
                                        .withBekreftelse(new ArrayList<>())
                                )
                                .withOversikt(new JsonOkonomioversikt()
                                        .withInntekt(new ArrayList<>())
                                        .withUtgift(new ArrayList<>())
                                        .withFormue(new ArrayList<>())
                                )
                        )
                )
                .withMottaker(new JsonSoknadsmottaker()
                        .withNavEnhetsnavn("")
                        .withEnhetsnummer(""))
                .withDriftsinformasjon(new JsonDriftsinformasjon()
                        .withUtbetalingerFraNavFeilet(false)
                        .withInntektFraSkatteetatenFeilet(false)
                        .withStotteFraHusbankenFeilet(false))
                .withKompatibilitet(new ArrayList<>())
        ).withVedlegg(new JsonVedleggSpesifikasjon());
    }

    private Timer createDebugTimer(String name, String id) {
        Timer timer = MetricsFactory.createTimer("debug.startsoknad." + name);
        timer.addFieldToReport("soknadstype", SOKNAD_TYPE_PREFIX);
        timer.addFieldToReport("randomid", id);
        timer.start();
        return timer;
    }

    private SoknadMetadata.VedleggMetadataListe convertToVedleggMetadataListe(SoknadUnderArbeid soknadUnderArbeid) {
        final List<JsonVedlegg> jsonVedleggs = getVedleggFromInternalSoknad(soknadUnderArbeid);
        SoknadMetadata.VedleggMetadataListe vedlegg = new SoknadMetadata.VedleggMetadataListe();

        vedlegg.vedleggListe = jsonVedleggs.stream().map(SoknadService::mapJsonVedleggToVedleggMetadata).collect(Collectors.toList());

        return vedlegg;
    }

    private static SoknadMetadata.VedleggMetadata mapJsonVedleggToVedleggMetadata(JsonVedlegg jsonVedlegg) {
        SoknadMetadata.VedleggMetadata m = new SoknadMetadata.VedleggMetadata();
        m.skjema = jsonVedlegg.getType();
        m.tillegg = jsonVedlegg.getTilleggsinfo();
        m.filnavn = jsonVedlegg.getType();
        m.status = Vedleggstatus.valueOf(jsonVedlegg.getStatus());
        m.hendelseType = jsonVedlegg.getHendelseType();
        m.hendelseReferanse = jsonVedlegg.getHendelseReferanse();
        return m;
    }

}