package no.nav.sosialhjelp.soknad.nymodell.controller.dto

import java.time.LocalDateTime
import java.util.*

data class NySoknadDto (
    val soknadId: UUID
)
data class SoknadDto (
    val soknadId: UUID,
    val innsendingsTidspunkt: LocalDateTime? = null
)