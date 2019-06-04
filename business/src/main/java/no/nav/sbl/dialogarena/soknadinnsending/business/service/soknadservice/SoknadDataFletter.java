package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.metrics.MetricsFactory;
import no.nav.metrics.Timer;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadataListe;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
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
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon.SKJEMANUMMER;
import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon.SOKNAD_TYPE_PREFIX;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.JsonVedleggUtils.getVedleggFromInternalSoknad;
import static org.slf4j.LoggerFactory.getLogger;


@Component
public class SoknadDataFletter {

    private static final Logger logger = getLogger(SoknadDataFletter.class);

    @Inject
    private HenvendelseService henvendelseService;
    @Inject
    private OppgaveHandterer oppgaveHandterer;

    @Inject
    private SoknadMetricsService soknadMetricsService;

    @Inject
    private InnsendingService innsendingService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private SystemdataUpdater systemdata;

    @Transactional
    public String startSoknad() {
        String mainUid = randomUUID().toString();

        Timer startTimer = createDebugTimer("startTimer", mainUid);

        String aktorId = OidcFeatureToggleUtils.getUserId();
        Timer henvendelseTimer = createDebugTimer("startHenvendelse", mainUid);
        String behandlingsId = henvendelseService.startSoknad(aktorId, SKJEMANUMMER, SoknadType.SEND_SOKNAD_KOMMUNAL);
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

        systemdata.update(soknadUnderArbeid);

        soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, aktorId);

        oprettIDbTimer.stop();
        oprettIDbTimer.report();
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

    private Timer createDebugTimer(String name, String id) {
        Timer timer = MetricsFactory.createTimer("debug.startsoknad." + name);
        timer.addFieldToReport("soknadstype", SOKNAD_TYPE_PREFIX);
        timer.addFieldToReport("randomid", id);
        timer.start();
        return timer;
    }

    public void sendSoknad(String behandlingsId) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        if (soknadUnderArbeid.erEttersendelse() && getVedleggFromInternalSoknad(soknadUnderArbeid).isEmpty()){
            logger.error("Kan ikke sende inn ettersendingen med ID {0} uten å ha lastet opp vedlegg", behandlingsId);
            throw new ApplicationException("Kan ikke sende inn ettersendingen uten å ha lastet opp vedlegg");
        }
        logger.info("Starter innsending av søknad med behandlingsId {}", behandlingsId);

        VedleggMetadataListe vedlegg = convertToVedleggMetadataListe(soknadUnderArbeid);
        henvendelseService.oppdaterMetadataVedAvslutningAvSoknad(behandlingsId, vedlegg, soknadUnderArbeid);
        oppgaveHandterer.leggTilOppgave(behandlingsId, eier);

        innsendingService.opprettSendtSoknad(soknadUnderArbeid);

        soknadMetricsService.sendtSoknad(soknadUnderArbeid.erEttersendelse());
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

    private static SoknadMetadata.VedleggMetadata mapJsonVedleggToVedleggMetadata(JsonVedlegg jsonVedlegg) {
        SoknadMetadata.VedleggMetadata m = new SoknadMetadata.VedleggMetadata();
        m.skjema = jsonVedlegg.getType();
        m.tillegg = jsonVedlegg.getTilleggsinfo();
        m.filnavn = jsonVedlegg.getType();
        m.status = Vedleggstatus.Status.valueOf(jsonVedlegg.getStatus());
        return m;
    }

    private static void logAlderTilKibana(String eier) {
        if (eier != null && eier.length() == 11 && Integer.parseInt(eier.substring(0, 1)) < 4) {
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

    private static int calculateAge(LocalDate birthDate) {
        if (birthDate != null) {
            return Period.between(birthDate, LocalDate.now()).getYears();
        } else {
            return 0;
        }
    }
}
