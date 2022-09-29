package no.nav.sosialhjelp.soknad.db.repositories.oppgave

interface OppgaveRepository {
    fun opprett(oppgave: Oppgave)
    fun hentNeste(): Oppgave?
    fun hentOppgave(behandlingsId: String): Oppgave?
    fun hentOppgaveIdList(behandlingsIdList: List<String>): List<Long>
    fun oppdater(oppgave: Oppgave)
    fun hentAntallFeilede(): Int
    fun hentAntallStuckUnderArbeid(): Int
    fun retryOppgaveStuckUnderArbeid(): Int
    fun slettOppgaver(oppgaveIdList: List<Long>)
    fun count(): Int
}
