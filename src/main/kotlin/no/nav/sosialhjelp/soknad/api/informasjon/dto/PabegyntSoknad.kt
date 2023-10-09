package no.nav.sosialhjelp.soknad.api.informasjon.dto

import java.time.LocalDateTime

data class PabegyntSoknad(
    private val sistOppdatert: LocalDateTime,
    val behandlingsId: String
)
