package no.nav.sosialhjelp.soknad.v2.soknad

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/begrunnelse", produces = [MediaType.APPLICATION_JSON_VALUE])
class BegrunnelseController(
    private val service: BegrunnelseService,
) {
    @GetMapping
    fun getBegrunnelse(
        @PathVariable("soknadId") soknadId: UUID,
    ): BegrunnelseDto {
        return service.findBegrunnelse(soknadId).toBegrunnelseDto()
    }

    @PutMapping
    fun updateBegrunnelse(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) input: BegrunnelseInput,
    ): BegrunnelseDto {
        // TODO Trengs forskjellig håndtering av disse?
        return when (input) {
            is HarHvaSokesOmInput -> service.updateHvaSokesOm(soknadId, input.hvorforSoke, input.hvaSokesOm)
            is HarKategorierInput -> service.updateKategorier(soknadId, input.hvorforSoke, input.kategorier)
        }
            .toBegrunnelseDto()
    }
}

data class BegrunnelseDto(
    val hvaSokesOm: String = "",
    val hvorforSoke: String = "",
    val kategorier: Set<Kategori>? = null,
)

fun Begrunnelse.toBegrunnelseDto(): BegrunnelseDto {
    return BegrunnelseDto(
        hvorforSoke = hvorforSoke,
        hvaSokesOm = hvaSokesOm,
    )
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = HarHvaSokesOmInput::class, name = "HarHvaSokesOm"),
    JsonSubTypes.Type(value = HarKategorierInput::class, name = "HarKategorier"),
)
sealed interface BegrunnelseInput

data class HarHvaSokesOmInput(
    val hvorforSoke: String,
    val hvaSokesOm: String,
) : BegrunnelseInput

data class HarKategorierInput(
    val hvorforSoke: String,
    val kategorier: Set<Kategori>,
) : BegrunnelseInput
