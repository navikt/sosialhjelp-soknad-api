package no.nav.sosialhjelp.soknad.db.repositories.oppgave

import java.util.Optional

interface OppgaveRepository {
    fun opprett(oppgave: Oppgave)
    fun hentNeste(): Optional<Oppgave>
    fun hentOppgave(behandlingsId: String): Optional<Oppgave>
    fun oppdater(oppgave: Oppgave)
    fun hentStatus(): Map<String, Int>
    fun retryOppgaveStuckUnderArbeid(): Int
    fun slettOppgave(behandlingsId: String)
}
