package no.nav.sosialhjelp.soknad.business.batch;

import no.nav.sosialhjelp.metrics.MetricsFactory;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SlettSoknadUnderArbeidScheduler {

    private static final Logger logger = getLogger(SlettSoknadUnderArbeidScheduler.class);

    private static final String KLOKKEN_HALV_FEM_OM_NATTEN = "0 30 4 * * *";
    private static final int SCHEDULE_INTERRUPT_S = 60 * 10;
    private static final int DAGER_GAMMELT = 15;

    private LocalDateTime batchStartTime;
    private int vellykket;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Scheduled(cron = KLOKKEN_HALV_FEM_OM_NATTEN)
    public void slettGamleSoknadUnderArbeid() {
        if (ServiceUtils.isScheduledTasksDisabled()) {
            logger.warn("Scheduler is disabled");
            return;
        }

        batchStartTime = LocalDateTime.now();
        vellykket = 0;

        if (Boolean.parseBoolean(System.getProperty("sendsoknad.batch.enabled", "true"))) {
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

    private void slett() {
        var soknader = soknadUnderArbeidRepository.hentSoknaderForBatch();

        soknader.forEach(soknadUnderArbeid -> {
            if (harGaattForLangTid()) {
                logger.warn("Jobben har kjørt i mer enn {} s. Den blir derfor terminert", SCHEDULE_INTERRUPT_S);
                return;
            }

            // kommentert ut selve slettingen
//            soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid, soknadUnderArbeid.getEier());
            vellykket++;
        });

    }

    private boolean harGaattForLangTid() {
        return LocalDateTime.now().isAfter(batchStartTime.plusSeconds(SCHEDULE_INTERRUPT_S));
    }
}
