package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave;

import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.Oppgave.Status;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.MetadataInnfyller;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.oppgave.OppgaveRepository;
import org.slf4j.Logger;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.lang.Math.pow;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@Import({
        FiksHandterer.class,
        MetadataInnfyller.class
})
public class OppgaveHandterer {

    private static final Logger logger = getLogger(OppgaveHandterer.class);
    private static final int FEIL_THRESHOLD = 20;

    @Inject
    private
    FiksHandterer fiksHandterer;

    @Inject
    private
    OppgaveRepository oppgaveRepository;

    @Scheduled(fixedDelay = 10000)
    public void prosesserOppgaver() {
        while (true) {
            Optional<Oppgave> oppgaveOptional = oppgaveRepository.hentNeste();

            if (!oppgaveOptional.isPresent()) {
                return;
            }

            Oppgave oppgave = oppgaveOptional.get();

            try {
                fiksHandterer.eksekver(oppgave);
            } catch (Exception e) {
                logger.error("Oppgave feilet, id: {}, beh: {}", oppgave.id, oppgave.behandlingsId, e);
                oppgaveFeilet(oppgave);
            }

            if (oppgave.status == Status.UNDER_ARBEID) {
                oppgave.status = Status.KLAR;
            }
            oppgaveRepository.oppdater(oppgave);
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

    public void leggTilOppgave(String behandlingsId) {
        Oppgave oppgave = new Oppgave();
        oppgave.behandlingsId = behandlingsId;
        oppgave.type = FiksHandterer.FIKS_OPPGAVE;
        oppgave.status = Status.KLAR;
        oppgave.opprettet = LocalDateTime.now();
        oppgave.nesteForsok = LocalDateTime.now();
        oppgave.steg = 0;

        // TODO id

        oppgaveRepository.opprett(oppgave);
    }
}
