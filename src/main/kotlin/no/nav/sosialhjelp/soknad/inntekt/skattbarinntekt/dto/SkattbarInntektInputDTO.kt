package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto

import jakarta.validation.constraints.NotNull

data class SkattbarInntektInputDTO(
    @NotNull
    val samtykke: Boolean,
)