package no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad

interface BatchSendtSoknadRepository {
    fun hentSendtSoknad(behandlingsId: String): Long?
    fun slettSendtSoknad(sendtSoknadId: Long)
}
