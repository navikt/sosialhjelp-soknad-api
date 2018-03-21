package no.nav.sbl.dialogarena.soknadinnsending.business.db.oppgave;

import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.Oppgave;

import java.util.Optional;

public interface OppgaveRepository {

    void opprett(Oppgave oppgave);

    Optional<Oppgave> hentNeste();

    void oppdater(Oppgave oppgave);


}
