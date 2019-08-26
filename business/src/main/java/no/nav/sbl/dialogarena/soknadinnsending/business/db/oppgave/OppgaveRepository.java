package no.nav.sbl.dialogarena.soknadinnsending.business.db.oppgave;

import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.Oppgave;

import java.util.Map;
import java.util.Optional;

public interface OppgaveRepository {

    void opprett(Oppgave oppgave);

    Optional<Oppgave> hentNeste();

    Optional<Oppgave> hentOppgave(String behandlingsId);

    void oppdater(Oppgave oppgave);

    Map<String, Integer> hentStatus();

    int retryOppgaveStuckUnderArbeid();

    void slettOppgave(String behandlingsId);
}
