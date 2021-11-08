package no.nav.sosialhjelp.soknad.business.batch;

import no.nav.sosialhjelp.metrics.MetricsFactory;
import no.nav.sosialhjelp.metrics.Timer;
import no.nav.sosialhjelp.soknad.business.batch.oppgave.Oppgave;
import no.nav.sosialhjelp.soknad.business.db.repositories.oppgave.OppgaveRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.sendtsoknad.BatchSendtSoknadRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.BatchSoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.client.leaderelection.LeaderElection;
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class SlettLoggScheduler {

    private static final Logger logger = getLogger(SlettLoggScheduler.class);

    private static final String KLOKKEN_FEM_OM_NATTEN = "0 0 5 * * *";
    private static final int SCHEDULE_INTERRUPT_S = 60 * 30; // 30 min
    private static final int DAGER_GAMMELT = 365; // Ett år

    private LocalDateTime batchStartTime;
    private int vellykket;

    private final LeaderElection leaderElection;
    private final BatchSoknadMetadataRepository batchSoknadMetadataRepository;
    private final BatchSendtSoknadRepository batchSendtSoknadRepository;
    private final OppgaveRepository oppgaveRepository;
    private final boolean batchEnabled;

    public SlettLoggScheduler(
            LeaderElection leaderElection,
            BatchSoknadMetadataRepository batchSoknadMetadataRepository,
            BatchSendtSoknadRepository batchSendtSoknadRepository,
            OppgaveRepository oppgaveRepository,
            @Value("${sendsoknad.batch.enabled}") boolean batchEnabled
    ) {
        this.leaderElection = leaderElection;
        this.batchSoknadMetadataRepository = batchSoknadMetadataRepository;
        this.batchSendtSoknadRepository = batchSendtSoknadRepository;
        this.oppgaveRepository = oppgaveRepository;
        this.batchEnabled = batchEnabled;
    }

    @Scheduled(cron = KLOKKEN_FEM_OM_NATTEN)
    public void slettLogger() {
        if (ServiceUtils.isScheduledTasksDisabled()) {
            logger.warn("Scheduler is disabled");
            return;
        }

        if (leaderElection.isLeader()) {
            batchStartTime = LocalDateTime.now();
            vellykket = 0;
            if (batchEnabled) {
                logger.info("Starter sletting av logger for ett år gamle søknader");
                Timer batchTimer = MetricsFactory.createTimer("sosialhjelp.debug.slettLogg");
                batchTimer.start();

                try {
                    slettForeldetLogg();
                } catch (RuntimeException e) {
                    logger.error("Batchjobb feilet for sletting av logg", e);
                    batchTimer.setFailed();
                } finally {
                    batchTimer.stop();
                    batchTimer.addFieldToReport("vellykket", vellykket);
                    batchTimer.report();
                    logger.info("Jobb fullført for sletting av logg: {} vellykket", vellykket);
                }

            } else {
                logger.warn("Batch disabled. Må sette environment property sendsoknad.batch.enabled til true for å sette den på igjen");
            }
        }
    }

    private void slettForeldetLogg() {
        Optional<SoknadMetadata> soknad = batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMELT);

        while (soknad.isPresent()) {
            SoknadMetadata soknadMetadata = soknad.get();

            String behandlingsId = soknadMetadata.behandlingsId;

            Optional<Long> sendtSoknadIdOptional = batchSendtSoknadRepository.hentSendtSoknad(behandlingsId);
            sendtSoknadIdOptional.ifPresent(sendtSoknadId -> batchSendtSoknadRepository.slettSendtSoknad(sendtSoknadId));

            Optional<Oppgave> oppgaveOptional = oppgaveRepository.hentOppgave(behandlingsId);
            oppgaveOptional.ifPresent(oppgave -> oppgaveRepository.slettOppgave(behandlingsId));

            batchSoknadMetadataRepository.slettSoknadMetaData(behandlingsId);

            vellykket++;

            if (harGaattForLangTid()) {
                logger.warn("Jobben har kjørt i mer enn {} s. Den blir derfor terminert", SCHEDULE_INTERRUPT_S);
                return;
            }
            soknad = batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMELT);
        }

    }

    private boolean harGaattForLangTid() {
        return LocalDateTime.now().isAfter(batchStartTime.plusSeconds(SCHEDULE_INTERRUPT_S));
    }
}

