package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.metrics.MetricsFactory;
import no.nav.metrics.Timer;
import no.nav.sbl.dialogarena.sendsoknad.domain.PersonAlder;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SosialhjelpSoknadApiException;
import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandlerWrapper;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.BostotteSystemdata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.SkattetatenSystemdata;
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
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon.SOKNAD_TYPE_PREFIX;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.JsonVedleggUtils.getVedleggFromInternalSoknad;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SoknadService {

    private static final Logger logger = getLogger(SoknadService.class);

    @Inject
    private HenvendelseService henvendelseService;

    @Inject
    private OppgaveHandterer oppgaveHandterer;

    @Inject
    private SoknadMetricsService soknadMetricsService;

    @Inject
    private InnsendingService innsendingService;

    @Inject
    private EttersendingService ettersendingService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private SystemdataUpdater systemdataUpdater;

    @Inject
    private BostotteSystemdata bostotteSystemdata;

    @Inject
    private SkattetatenSystemdata skattetatenSystemdata;

    @Inject
    private TextService textService;

    @Inject
    private SubjectHandlerWrapper subjectHandlerWrapper;

    @Transactional
    public String startSoknad(String token) {
        String mainUid = randomUUID().toString();

        Timer startTimer = createDebugTimer("startTimer", mainUid);

        String aktorId = subjectHandlerWrapper.getIdent();
        Timer henvendelseTimer = createDebugTimer("startHenvendelse", mainUid);
        String behandlingsId = henvendelseService.startSoknad(aktorId);
        henvendelseTimer.stop();
        henvendelseTimer.report();

        soknadMetricsService.startetSoknad(false);

        Timer oprettIDbTimer = createDebugTimer("oprettIDb", mainUid);

        final SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withVersjon(1L)
                .withEier(aktorId)
                .withBehandlingsId(behandlingsId)
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(aktorId))
                .withInnsendingStatus(SoknadInnsendingStatus.UNDER_ARBEID)
                .withOpprettetDato(LocalDateTime.now())
                .withSistEndretDato(LocalDateTime.now());

        systemdataUpdater.update(soknadUnderArbeid, token);

        soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, aktorId);

        oprettIDbTimer.stop();
        oprettIDbTimer.report();
        startTimer.stop();
        startTimer.report();

        return behandlingsId;
    }

    @Transactional
    public void sendSoknad(String behandlingsId) {
        final String eier = subjectHandlerWrapper.getIdent();
        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        if (soknadUnderArbeid.erEttersendelse() && getVedleggFromInternalSoknad(soknadUnderArbeid).isEmpty()) {
            logger.error("Kan ikke sende inn ettersendingen med ID {} uten å ha lastet opp vedlegg", behandlingsId);
            throw new SosialhjelpSoknadApiException("Kan ikke sende inn ettersendingen uten å ha lastet opp vedlegg");
        }
        logger.info("Starter innsending av søknad med behandlingsId {}", behandlingsId);
        if(!soknadUnderArbeid.erEttersendelse()) {
            if (soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getStotteFraHusbankenFeilet()) {
                logger.info("Nedlasting fra Husbanken har feilet for innsendtsoknad." +
                        finnAlderPaaDataFor(soknadUnderArbeid, BOSTOTTE_SAMTYKKE));
            }
            if (soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getInntektFraSkatteetatenFeilet()) {
                logger.info("Nedlasting fra Skatteetaten har feilet for innsendtsoknad." +
                        finnAlderPaaDataFor(soknadUnderArbeid, UTBETALING_SKATTEETATEN_SAMTYKKE));
            }
        }

        SoknadMetadata.VedleggMetadataListe vedlegg = convertToVedleggMetadataListe(soknadUnderArbeid);
        henvendelseService.oppdaterMetadataVedAvslutningAvSoknad(behandlingsId, vedlegg, soknadUnderArbeid, false);
        oppgaveHandterer.leggTilOppgave(behandlingsId, eier);

        innsendingService.opprettSendtSoknad(soknadUnderArbeid);

        soknadMetricsService.sendtSoknad(soknadUnderArbeid.erEttersendelse());
        if (!soknadUnderArbeid.erEttersendelse() && !MockUtils.isTillatMockRessurs()) {
            logAlderTilKibana(eier);
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
        String eier = subjectHandlerWrapper.getIdent();
        Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknadOptional(behandlingsId, eier);
        if (soknadUnderArbeidOptional.isPresent()) {
            soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeidOptional.get(), eier);
            henvendelseService.avbrytSoknad(soknadUnderArbeidOptional.get().getBehandlingsId(), false);
            soknadMetricsService.avbruttSoknad(soknadUnderArbeidOptional.get().erEttersendelse());
        }
    }

    @Transactional
    public void oppdaterSamtykker(String behandlingsId, boolean harBostotteSamtykke, boolean harSkatteetatenSamtykke, String token) {
        final String eier = subjectHandlerWrapper.getIdent();
        final SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        if (harSkatteetatenSamtykke) {
            skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid, token);
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
        return m;
    }

    private static void logAlderTilKibana(String eier) {
        int age = new PersonAlder(eier).getAlder();
        if (age > 0 && age < 30) {
            logger.info("DIGISOS-1164: UNDER30 - Soknad sent av bruker med alder: " + age);
        } else {
            logger.info("DIGISOS-1164: OVER30 - Soknad sent av bruker med alder:" + age);
        }
    }
}