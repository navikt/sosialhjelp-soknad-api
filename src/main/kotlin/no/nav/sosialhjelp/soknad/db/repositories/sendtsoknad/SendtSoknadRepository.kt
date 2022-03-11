package no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad

import java.util.Optional

interface SendtSoknadRepository {
    fun opprettSendtSoknad(sendtSoknad: SendtSoknad, eier: String?): Long?
    fun hentSendtSoknad(behandlingsId: String, eier: String?): Optional<SendtSoknad>
    fun oppdaterSendtSoknadVedSendingTilFiks(fiksforsendelseId: String?, behandlingsId: String?, eier: String?)
}
