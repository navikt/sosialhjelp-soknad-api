package no.nav.sosialhjelp.soknad.v2.soknad

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema
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
            is HarKategorierInput -> service.updateKategorier(soknadId, input.kategorier, input.annet)
        }.toBegrunnelseDto()
    }
}

data class BegrunnelseDto(
    val hvaSokesOm: String = "",
    val hvorforSoke: String? = "",
    val kategorier: KategorierDto = KategorierDto(),
)

data class KategorierDto(
    val definerte: Set<Kategori> = emptySet(),
    val annet: String = "",
)

fun Begrunnelse.toBegrunnelseDto(): BegrunnelseDto {
    return BegrunnelseDto(
        hvorforSoke = hvorforSoke,
        hvaSokesOm = hvaSokesOm,
        kategorier =
            KategorierDto(
                definerte = kategorier.definerte,
                annet = kategorier.annet,
            ),
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
@Schema(
    discriminatorProperty = "type",
    discriminatorMapping = [
        DiscriminatorMapping(value = "HarHvaSokesOm", schema = HarHvaSokesOmInput::class),
        DiscriminatorMapping(value = "HarKategorier", schema = HarKategorierInput::class),
    ],
)
sealed class BegrunnelseInput()

class HarHvaSokesOmInput(
    val hvorforSoke: String?,
    val hvaSokesOm: String,
) : BegrunnelseInput()

class HarKategorierInput(
    val kategorier: Set<Kategori>,
    val annet: String,
) : BegrunnelseInput()
