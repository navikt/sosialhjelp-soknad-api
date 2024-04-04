package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

data class SkattbarInntektInputDTO(
    @NotNull
    @Schema(description = "Samtykke til å hente skattbar inntekt fra skatteetaten", example = "true", nullable = true)
    val samtykke: Boolean
)
