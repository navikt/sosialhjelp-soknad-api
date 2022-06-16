package no.nav.sosialhjelp.soknad.db.repositories.oppgave

interface OppgaveRepository {
    fun opprett(oppgave: Oppgave)
    fun hentNeste(): Oppgave?
    fun hentOppgave(behandlingsId: String): Oppgave?
    fun oppdater(oppgave: Oppgave)
    fun hentAntallFeilede(): Int
    fun hentAntallStuckUnderArbeid(): Int
    fun retryOppgaveStuckUnderArbeid(): Int
    fun slettOppgave(behandlingsId: String)
    fun count(): Int
}
