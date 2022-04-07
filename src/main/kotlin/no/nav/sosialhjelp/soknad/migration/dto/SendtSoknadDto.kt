package no.nav.sosialhjelp.soknad.migration.dto

import java.time.LocalDateTime

data class SendtSoknadDto(
    val sendtSoknadId: Long,
    val behandlingsId: String,
    val tilknyttetBehandlingsId: String?,
    val eier: String,
    val fiksforsendelseId: String?,
    val orgnummer: String,
    val navEnhetsnavn: String,
    val brukerOpprettetDato: LocalDateTime,
    val brukerFerdigDato: LocalDateTime,
    val sendtDato: LocalDateTime?
)
