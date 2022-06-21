package no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad

interface BatchSendtSoknadRepository {
    fun hentSendtSoknadIdList(behandlingsIdList: List<String>): List<Long>
    fun slettSendtSoknader(sendtSoknadIdList: List<Long>)
}
