package no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad

import java.util.Optional

interface BatchSendtSoknadRepository {
    fun hentSendtSoknad(behandlingsId: String): Optional<Long>
    fun slettSendtSoknad(sendtSoknadId: Long)
}
