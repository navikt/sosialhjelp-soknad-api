package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.metrics.MetricsFactory;
import no.nav.metrics.Timer;
import no.nav.sbl.dialogarena.common.suspend.SuspendServlet;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;

import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.AVBRUTT_AUTOMATISK;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class AvbrytAutomatiskSheduler {

    private static final Logger logger = getLogger(AvbrytAutomatiskSheduler.class);

    private static final String KLOKKEN_FIRE_OM_NATTEN = "0 0 4 * * *";
    private static final int SCHEDULE_INTERRUPT_S = 60 * 10;
    private static final int DAGER_GAMMELT = 7 * 2;

    private LocalDateTime batchStartTime;
    private int vellykket;

    @Inject
    private SoknadMetadataRepository soknadMetadataRepository;
    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Scheduled(cron = KLOKKEN_FIRE_OM_NATTEN)
    public void avbrytGamleSoknader() throws InterruptedException {
        if (ServiceUtils.isScheduledTasksDisabled()) {
            logger.warn("Scheduler is disabled");
            return;
        }

        batchStartTime = LocalDateTime.now();
        vellykket = 0;
        if (Boolean.valueOf(System.getProperty("sendsoknad.batch.enabled", "true"))) {
            logger.info("Starter avbryting av gamle søknader");
            Timer batchTimer = MetricsFactory.createTimer("sosialhjelp.debug.avbryt");
            batchTimer.start();

            try {
                avbryt();
            } catch (RuntimeException e) {
                logger.error("Batchjobb feilet", e);
                batchTimer.setFailed();
            } finally {
                batchTimer.stop();
                batchTimer.addFieldToReport("vellykket", vellykket);
                batchTimer.report();
                logger.info("Jobb fullført: {} vellykket", vellykket);
            }

        } else {
            logger.warn("Batch disabled. Må sette environment property sendsoknad.batch.enabled til true for å sette den på igjen");
        }
    }

    private void avbryt() {
        Optional<SoknadMetadata> soknad = soknadMetadataRepository.hentForBatch(DAGER_GAMMELT);

        while (soknad.isPresent()) {
            SoknadMetadata soknadMetadata = soknad.get();
            soknadMetadata.status = AVBRUTT_AUTOMATISK;
            soknadMetadata.sistEndretDato = LocalDateTime.now();
            soknadMetadataRepository.oppdater(soknadMetadata);

            final String behandlingsId = soknadMetadata.behandlingsId;
            final String eier = soknadMetadata.fnr;

            Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknadOptional(behandlingsId, eier);
            soknadUnderArbeidOptional.ifPresent(soknadUnderArbeid -> soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid, eier));

            soknadMetadataRepository.leggTilbakeBatch(soknadMetadata.id);
            vellykket++;

            if (harGaattForLangTid()) {
                logger.warn("Jobben har kjørt i mer enn {} s. Den blir derfor terminert", SCHEDULE_INTERRUPT_S);
                return;
            }
            if (!SuspendServlet.isRunning()) {
                logger.warn("Avbryter jobben da appen skal suspendes");
                return;
            }
            soknad = soknadMetadataRepository.hentForBatch(DAGER_GAMMELT);
        }

    }

    private boolean harGaattForLangTid() {
        return LocalDateTime.now().isAfter(batchStartTime.plusSeconds(SCHEDULE_INTERRUPT_S));
    }
}

