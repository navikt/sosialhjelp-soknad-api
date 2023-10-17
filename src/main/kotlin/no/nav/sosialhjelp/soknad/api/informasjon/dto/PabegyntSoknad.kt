package no.nav.sosialhjelp.soknad.api.informasjon.dto

import java.time.LocalDateTime

data class PabegyntSoknad(
    val sistOppdatert: LocalDateTime,
    val behandlingsId: String
)
