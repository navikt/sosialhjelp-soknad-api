package no.nav.sosialhjelp.soknad.business.batch;

import no.nav.sosialhjelp.metrics.MetricsFactory;
import no.nav.sosialhjelp.metrics.Timer;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.BatchSoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;

import static no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK;
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
    private BatchSoknadMetadataRepository batchSoknadMetadataRepository;
    @Inject
    private BatchSoknadUnderArbeidRepository batchSoknadUnderArbeidRepository;

    @Scheduled(cron = KLOKKEN_FIRE_OM_NATTEN)
    public void avbrytGamleSoknader() {
        if (ServiceUtils.isScheduledTasksDisabled()) {
            logger.warn("Scheduler is disabled");
            return;
        }

        batchStartTime = LocalDateTime.now();
        vellykket = 0;
        if (Boolean.parseBoolean(System.getProperty("sendsoknad.batch.enabled", "true"))) {
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
        Optional<SoknadMetadata> soknad = batchSoknadMetadataRepository.hentForBatch(DAGER_GAMMELT);

        while (soknad.isPresent()) {
            SoknadMetadata soknadMetadata = soknad.get();
            soknadMetadata.status = AVBRUTT_AUTOMATISK;
            soknadMetadata.sistEndretDato = LocalDateTime.now();
            soknadMetadataRepository.oppdater(soknadMetadata);

            final String behandlingsId = soknadMetadata.behandlingsId;

            Optional<Long> soknadUnderArbeidOptional = batchSoknadUnderArbeidRepository.hentSoknadUnderArbeidIdFromBehandlingsIdOptional(behandlingsId);
            soknadUnderArbeidOptional.ifPresent(soknadUnderArbeid -> batchSoknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid));

            batchSoknadMetadataRepository.leggTilbakeBatch(soknadMetadata.id);
            vellykket++;

            if (harGaattForLangTid()) {
                logger.warn("Jobben har kjørt i mer enn {} s. Den blir derfor terminert", SCHEDULE_INTERRUPT_S);
                return;
            }
            soknad = batchSoknadMetadataRepository.hentForBatch(DAGER_GAMMELT);
        }

    }

    private boolean harGaattForLangTid() {
        return LocalDateTime.now().isAfter(batchStartTime.plusSeconds(SCHEDULE_INTERRUPT_S));
    }
}

