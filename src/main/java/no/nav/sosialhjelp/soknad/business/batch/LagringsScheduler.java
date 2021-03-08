package no.nav.sosialhjelp.soknad.business.batch;

import no.nav.sosialhjelp.metrics.MetricsFactory;
import no.nav.sosialhjelp.metrics.Timer;
import no.nav.sosialhjelp.soknad.business.service.HenvendelseService;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class LagringsScheduler {

    private static final Logger logger = getLogger(LagringsScheduler.class);
    private static final int SCHEDULE_RATE_MS = 1000 * 60 * 60; // 1 time
    private static final int SCHEDULE_INTERRUPT_MS = 1000 * 60 * 10; // 10 min
    private DateTime batchStartTime;
    private int vellykket;
    private int feilet;

    @Inject
    private HenvendelseService henvendelseService;
    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Scheduled(fixedRate = SCHEDULE_RATE_MS)
    public void slettForeldedeEttersendelserFraSoknadUnderArbeidDatabase() throws InterruptedException {
        if (ServiceUtils.isScheduledTasksDisabled()) {
            logger.warn("Scheduler is disabled");
            return;
        }

        batchStartTime = DateTime.now();
        vellykket = 0;
        feilet = 0;
        if (Boolean.parseBoolean(System.getProperty("sendsoknad.batch.enabled", "true"))) {
            logger.info("Starter flytting av søknader til henvendelse-jobb");
            Timer batchTimer = MetricsFactory.createTimer("debug.lagringsjobb");
            batchTimer.start();

            hentForeldedeEttersendelserFraDatabaseOgSlett(batchTimer);

            batchTimer.stop();
            batchTimer.addFieldToReport("vellykket", vellykket);
            batchTimer.addFieldToReport("feilet", feilet);
            batchTimer.report();
            logger.info("Jobb fullført: {} vellykket, {} feilet", vellykket, feilet);
        } else {
            logger.warn("Batch disabled. Må sette environment property sendsoknad.batch.enabled til true for å sette den på igjen");
        }
    }

    private void hentForeldedeEttersendelserFraDatabaseOgSlett(Timer metrikk) throws InterruptedException {
        List<SoknadUnderArbeid> soknadUnderArbeidList = soknadUnderArbeidRepository.hentForeldedeEttersendelser();
        for (SoknadUnderArbeid soknadUnderArbeid : soknadUnderArbeidList) {
            if (soknadUnderArbeid.erEttersendelse()) {
                avbrytOgSlettEttersendelse(soknadUnderArbeid);

                // Avslutt prosessen hvis det er gått for lang tid. Tyder på at noe er nede.
                if (harGaattForLangTid()) {
                    logger.warn("Jobben har kjørt i mer enn {} ms. Den blir derfor terminert", SCHEDULE_INTERRUPT_MS);
                    metrikk.addFieldToReport("avbruttPgaTid", true);
                    return;
                }
            } else {
                logger.warn("hentForeldedeEttersendelser har returnet soknadUnderArbeid som ikke er ettersendelse");
            }
        }
    }

    private void avbrytOgSlettEttersendelse(SoknadUnderArbeid soknadUnderArbeid) throws InterruptedException {
        try {
            henvendelseService.avbrytSoknad(soknadUnderArbeid.getBehandlingsId(), true);
            soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid, soknadUnderArbeid.getEier());

            vellykket++;
        } catch (Exception e) {
            feilet++;
            logger.error("Avbryt feilet for ettersending {}.", soknadUnderArbeid.getSoknadId(), e);
            Thread.sleep(1000); // Så loggen ikke blir fylt opp

        }
    }

    private boolean harGaattForLangTid() {
        return DateTime.now().isAfter(batchStartTime.plusMillis(SCHEDULE_INTERRUPT_MS));
    }
}

