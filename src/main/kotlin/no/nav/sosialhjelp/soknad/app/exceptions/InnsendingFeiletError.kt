package no.nav.sosialhjelp.soknad.app.exceptions

import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@Schema(
    discriminatorProperty = "type",
    discriminatorMapping = [
        DiscriminatorMapping(value = "InnsendingFeilet", schema = InnsendingFeiletError::class),
    ],
)
data class InnsendingFeiletError(
    val deletionDate: String,
)
