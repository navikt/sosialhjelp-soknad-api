package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.metrics.MetricsFactory;
import no.nav.metrics.Timer;
import no.nav.sbl.dialogarena.common.suspend.SuspendServlet;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.Oppgave;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.oppgave.OppgaveRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.sosialhjelp.domain.SendtSoknad;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class SlettLoggScheduler {

    private static final Logger logger = getLogger(SlettLoggScheduler.class);

    private static final String KLOKKEN_FEM_OM_NATTEN = "0 30 13 * * *";
    private static final int SCHEDULE_INTERRUPT_S = 60 * 10;
    private static final int DAGER_GAMMELT = 365; // Ett år

    private LocalDateTime batchStartTime;
    private int vellykket;

    @Inject
    private SoknadMetadataRepository soknadMetadataRepository;
    @Inject
    private SendtSoknadRepository sendtSoknadRepository;
    @Inject
    private OppgaveRepository oppgaveRepository;

    @Scheduled(cron = KLOKKEN_FEM_OM_NATTEN)
    public void slettLogger() {
        if (ServiceUtils.isScheduledTasksDisabled()) {
            logger.warn("Scheduler is disabled");
            return;
        }

        batchStartTime = LocalDateTime.now();
        vellykket = 0;
        if (Boolean.valueOf(System.getProperty("sendsoknad.batch.enabled", "true"))) {
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

    private void slettForeldetLogg() {
        Optional<SoknadMetadata> soknad = soknadMetadataRepository.hentAlleEldreEnn(DAGER_GAMMELT);

        while (soknad.isPresent()) {
            SoknadMetadata soknadMetadata = soknad.get();

            String behandlingsId = soknadMetadata.behandlingsId;
            String eier = soknadMetadata.fnr;

            Optional<SendtSoknad> sendtSoknadOptional = sendtSoknadRepository.hentSendtSoknad(behandlingsId, eier);
            sendtSoknadOptional.ifPresent(sendtSoknad -> sendtSoknadRepository.slettSendtSoknad(sendtSoknad, eier));

            Optional<Oppgave> oppgaveOptional = oppgaveRepository.hentOppgave(behandlingsId, eier);
            oppgaveOptional.ifPresent(oppgave -> oppgaveRepository.slettOppgave(behandlingsId, eier));

            soknadMetadataRepository.slettSoknadMetaData(behandlingsId, eier);

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

