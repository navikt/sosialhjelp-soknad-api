package no.nav.sosialhjelp.soknad.business.batch.oppgave;

import no.nav.sosialhjelp.metrics.Event;
import no.nav.sosialhjelp.metrics.MetricsFactory;
import no.nav.sosialhjelp.soknad.business.batch.oppgave.Oppgave.Status;
import no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks.FiksHandterer;
import no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks.FiksSender;
import no.nav.sosialhjelp.soknad.business.db.repositories.oppgave.OppgaveRepository;
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils;
import org.slf4j.Logger;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static java.lang.Math.pow;
import static no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations.MDC_BEHANDLINGS_ID;
import static no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations.putToMDC;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@Import({
        FiksHandterer.class,
        FiksSender.class
})
public class OppgaveHandtererImpl implements OppgaveHandterer {

    public static final int FORSTE_STEG_NY_INNSENDING = 21;

    private static final Logger logger = getLogger(OppgaveHandtererImpl.class);

    private static final int FEIL_THRESHOLD = 20;
    private static final int PROSESS_RATE = 10 * 1000; // 10 sek etter forrige
    private static final int RAPPORTER_RATE = 15 * 60 * 1000; // hvert kvarter
    private static final int RETRY_STUCK_RATE = 15 * 60 * 1000; // hvert kvarter

    private final FiksHandterer fiksHandterer;
    private final OppgaveRepository oppgaveRepository;

    public OppgaveHandtererImpl(FiksHandterer fiksHandterer, OppgaveRepository oppgaveRepository) {
        this.fiksHandterer = fiksHandterer;
        this.oppgaveRepository = oppgaveRepository;
    }

    @Scheduled(fixedDelay = PROSESS_RATE)
    public void prosesserOppgaver() {
        if (ServiceUtils.isScheduledTasksDisabled()) {
            logger.info("Scheduler is disabled");
            return;
        }

        while (true) {
            Optional<Oppgave> oppgaveOptional = oppgaveRepository.hentNeste();

            if (oppgaveOptional.isEmpty()) {
                return;
            }

            Oppgave oppgave = oppgaveOptional.get();
            Event event = MetricsFactory.createEvent("digisos.oppgaver");
            event.addTagToReport("oppgavetype", oppgave.type);
            event.addTagToReport("steg", oppgave.steg + "");
            event.addFieldToReport("behandlingsid", oppgave.behandlingsId);

            putToMDC(MDC_BEHANDLINGS_ID, oppgave.behandlingsId);

            try {
                fiksHandterer.eksekver(oppgave);
            } catch (Exception e) {
                logger.error("Oppgave feilet, id: {}, beh: {}", oppgave.id, oppgave.behandlingsId, e);
                oppgaveFeilet(oppgave);
                event.setFailed();
            }
            event.report();

            if (oppgave.status == Status.UNDER_ARBEID) {
                oppgave.status = Status.KLAR;
            }
            oppgaveRepository.oppdater(oppgave);
        }

    }
    
    @Scheduled(fixedDelay = RETRY_STUCK_RATE)
    public void retryStuckUnderArbeid() {
        if (ServiceUtils.isScheduledTasksDisabled()) {
            logger.info("Scheduler is disabled");
            return;
        }

        try {
            final int antall = oppgaveRepository.retryOppgaveStuckUnderArbeid();
            if (antall > 0) {
                logger.info("Har satt {} oppgaver tilbake til KLAR etter at de lå for lenge som UNDER_ARBEID.", antall);
            }
        } catch (Exception e) {
            logger.error("Uventet feil ved oppdatering av oppgaver som er stuck i UNDER_ARBEID");
        }
        
    }

    @Scheduled(fixedRate = RAPPORTER_RATE)
    public void rapporterFeilede() {
        if (ServiceUtils.isScheduledTasksDisabled()) {
            logger.info("Scheduler is disabled");
            return;
        }

        Map<String, Integer> statuser = oppgaveRepository.hentStatus();

        for (Map.Entry<String, Integer> entry : statuser.entrySet()) {
            logger.info("Databasestatus for oppgaver: {} er {}", entry.getKey(), entry.getValue());
            Event event = MetricsFactory.createEvent("status.oppgave." + entry.getKey());
            event.addFieldToReport("antall", entry.getValue());
            event.report();
        }
    }

    private void oppgaveFeilet(Oppgave oppgave) {
        oppgave.retries++;
        if (oppgave.retries > FEIL_THRESHOLD) {
            oppgave.status = Status.FEILET;
        } else {
            oppgave.nesteForsok = nesteForsokEksponensiellBackoff(oppgave.retries);
        }
    }

    private LocalDateTime nesteForsokEksponensiellBackoff(int antallForsok) {
        LocalDateTime backoff = LocalDateTime.now().plusMinutes((int) pow(2, antallForsok));
        LocalDateTime max = LocalDateTime.now().plusHours(1);
        return backoff.isBefore(max) ? backoff : max;
    }

    @Override
    public void leggTilOppgave(String behandlingsId, String eier) {
        Oppgave oppgave = new Oppgave();
        oppgave.behandlingsId = behandlingsId;
        oppgave.type = FiksHandterer.FIKS_OPPGAVE;
        oppgave.status = Status.KLAR;
        oppgave.opprettet = LocalDateTime.now();
        oppgave.nesteForsok = LocalDateTime.now();
        oppgave.steg = FORSTE_STEG_NY_INNSENDING;
        oppgave.retries = 0;
        oppgave.oppgaveData.avsenderFodselsnummer = eier;

        oppgaveRepository.opprett(oppgave);
    }
}
