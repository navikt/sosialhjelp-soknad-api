package no.nav.sosialhjelp.soknad.business.batch;

import no.nav.sosialhjelp.metrics.MetricsFactory;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.client.leaderelection.LeaderElection;
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SlettSoknadUnderArbeidScheduler {

    private static final Logger logger = getLogger(SlettSoknadUnderArbeidScheduler.class);

    private static final String KLOKKEN_HALV_FEM_OM_NATTEN = "0 30 4 * * *";
    private static final int SCHEDULE_INTERRUPT_S = 60 * 10;

    private LocalDateTime batchStartTime;
    private int vellykket;

    private final LeaderElection leaderElection;
    private final BatchSoknadUnderArbeidRepository batchSoknadUnderArbeidRepository;
    private final boolean batchEnabled;

    public SlettSoknadUnderArbeidScheduler(
            LeaderElection leaderElection,
            BatchSoknadUnderArbeidRepository batchSoknadUnderArbeidRepository,
            @Value("${sendsoknad.batch.enabled}") boolean batchEnabled
    ) {
        this.leaderElection = leaderElection;
        this.batchSoknadUnderArbeidRepository = batchSoknadUnderArbeidRepository;
        this.batchEnabled = batchEnabled;
    }

    @Scheduled(cron = KLOKKEN_HALV_FEM_OM_NATTEN)
    public void slettGamleSoknadUnderArbeid() {
        if (ServiceUtils.isScheduledTasksDisabled()) {
            logger.warn("Scheduler is disabled");
            return;
        }

        if (leaderElection.isLeader()) {
            batchStartTime = LocalDateTime.now();
            vellykket = 0;

            if (batchEnabled) {
                logger.info("Starter sletting av soknadUnderArbeid som er eldre enn 14 dager");
                var batchTimer = MetricsFactory.createTimer("sosialhjelp.debug.slettSoknadUnderArbeid");
                batchTimer.start();

                try {
                    slett();
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
    }

    private void slett() {
        var soknadIdList = batchSoknadUnderArbeidRepository.hentGamleSoknadUnderArbeidForBatch();

        soknadIdList.forEach(soknadId -> {
            if (harGaattForLangTid()) {
                logger.warn("Jobben har kjørt i mer enn {} s. Den blir derfor terminert", SCHEDULE_INTERRUPT_S);
                return;
            }

            batchSoknadUnderArbeidRepository.slettSoknad(soknadId);
            vellykket++;
        });
    }

    private boolean harGaattForLangTid() {
        return LocalDateTime.now().isAfter(batchStartTime.plusSeconds(SCHEDULE_INTERRUPT_S));
    }
}
