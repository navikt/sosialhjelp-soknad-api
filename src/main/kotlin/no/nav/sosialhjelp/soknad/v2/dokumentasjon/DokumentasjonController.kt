package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/dokumentasjon/forventet")
class DokumentasjonController(
    private val handler: DokumentasjonStatusUseCaseHandler,
) {
    @GetMapping
    fun getForventetDokumentasjon(
        @PathVariable("soknadId") soknadId: UUID,
    ): ForventetDokumentasjonDto {
        return handler.findForventetDokumentasjon(soknadId)
            .map { it.toDokumentasjonDto() }
            .let { ForventetDokumentasjonDto(it) }
    }

    @PutMapping
    fun updateDokumentasjonStatus(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody input: DokumentasjonInput,
    ): ForventetDokumentasjonDto {
        handler.updateDokumentasjonStatus(soknadId = soknadId, type = input.type, hasLevert = input.hasLevert)

        return getForventetDokumentasjon(soknadId)
    }
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = InntektTypeDto::class, name = "InntektType"),
    JsonSubTypes.Type(value = UtgiftTypeDto::class, name = "UtgiftType"),
    JsonSubTypes.Type(value = FormueTypeDto::class, name = "FormueType"),
    JsonSubTypes.Type(value = AnnenDokumentasjonTypeDto::class, name = "AnnenDokumentasjonType"),
)
@Schema(
    discriminatorProperty = "type",
    discriminatorMapping = [
        DiscriminatorMapping(value = "FormueType", schema = FormueTypeDto::class),
        DiscriminatorMapping(value = "InntektType", schema = InntektTypeDto::class),
        DiscriminatorMapping(value = "UtgiftType", schema = UtgiftTypeDto::class),
        DiscriminatorMapping(value = "AnnenDokumentasjonType", schema = AnnenDokumentasjonTypeDto::class),
    ],
    subTypes = [InntektTypeDto::class, FormueTypeDto::class, UtgiftTypeDto::class, AnnenDokumentasjonTypeDto::class],
)
sealed interface OpplysningTypeDto {
    val value: OpplysningType
}

data class InntektTypeDto(override val value: InntektType) : OpplysningTypeDto

data class UtgiftTypeDto(override val value: UtgiftType) : OpplysningTypeDto

data class FormueTypeDto(override val value: FormueType) : OpplysningTypeDto

data class AnnenDokumentasjonTypeDto(override val value: AnnenDokumentasjonType) : OpplysningTypeDto

data class ForventetDokumentasjonDto(
    val dokumentasjon: List<DokumentasjonDto>,
)

data class DokumentasjonDto(
    val type: OpplysningType,
    val dokumentasjonStatus: DokumentasjonStatus,
    val dokumenter: List<DokumentDto>,
)

data class DokumentDto(
    val dokumentId: UUID,
    val filnavn: String,
)

private fun Dokumentasjon.toDokumentasjonDto(): DokumentasjonDto {
    return DokumentasjonDto(
        type = type,
        dokumentasjonStatus = status,
        dokumenter =
            dokumenter.map { dokument ->
                DokumentDto(dokumentId = dokument.dokumentId, filnavn = dokument.filnavn)
            },
    )
}

data class DokumentasjonInput(
    val type: OpplysningType,
    val hasLevert: Boolean,
)
