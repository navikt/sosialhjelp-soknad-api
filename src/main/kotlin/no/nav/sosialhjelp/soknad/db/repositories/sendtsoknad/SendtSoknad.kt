package no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad

import java.time.LocalDateTime

data class SendtSoknad(
    var sendtSoknadId: Long = 0L, // dummy id. SendtSoknadRepository.opprettSendtSoknad bruker next sequence value som id
    var behandlingsId: String,
    var tilknyttetBehandlingsId: String? = null,
    var eier: String,
    var fiksforsendelseId: String? = null,
    var orgnummer: String,
    var navEnhetsnavn: String,
    var brukerOpprettetDato: LocalDateTime,
    var brukerFerdigDato: LocalDateTime,
    var sendtDato: LocalDateTime? = null
) {
    val erEttersendelse: Boolean get() = !tilknyttetBehandlingsId.isNullOrEmpty()
}
