package no.nav.sosialhjelp.soknad.v2.okonomi.formue

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/inntekt/verdier")
class VerdiController(
    private val verdiService: VerdiService,
) {
    @GetMapping
    fun getVerdier(
        @PathVariable("soknadId") soknadId: UUID,
    ): VerdierDto {
        // TODO må først sjekke bekreftelse
        return verdiService.getVerdier(soknadId)?.let { verdier ->
            if (verdier.isEmpty()) {
                VerdierDto(bekreftelse = false)
            } else {
                verdier.toVerdierDto(hasBekreftelse = true)
            }
        } ?: VerdierDto()
    }

    @PutMapping
    fun updateVerdier(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) input: VerdierInput,
    ): VerdierDto {
        when (input) {
            is HarVerdierInput ->
                verdiService.updateVerdier(
                    soknadId = soknadId,
                    existingTypes = input.toTypeSet(),
                    beskrivelseAnnet = if (input.hasBeskrivelseVerdi) input.beskrivelseVerdi else null,
                )
            is HarIkkeVerdierInput -> verdiService.removeVerdier(soknadId)
            else -> error("Ukjent VerdiInput-type")
        }
        return getVerdier(soknadId = soknadId)
    }
}

private fun Set<Formue>.toVerdierDto(hasBekreftelse: Boolean): VerdierDto {
    return VerdierDto(
        bekreftelse = hasBekreftelse,
        hasBolig = any { it.type == FormueType.VERDI_BOLIG },
        hasCampingvogn = any { it.type == FormueType.VERDI_CAMPINGVOGN },
        hasKjoretoy = any { it.type == FormueType.VERDI_KJORETOY },
        hasFritidseiendom = any { it.type == FormueType.VERDI_FRITIDSEIENDOM },
        hasAnnetVerdi = any { it.type == FormueType.VERDI_ANNET },
        beskrivelseVerdi = find { it.type == FormueType.VERDI_ANNET }?.beskrivelse,
    )
}

data class VerdierDto(
    val bekreftelse: Boolean? = null,
    val hasBolig: Boolean = false,
    val hasCampingvogn: Boolean = false,
    val hasKjoretoy: Boolean = false,
    val hasFritidseiendom: Boolean = false,
    val hasAnnetVerdi: Boolean = false,
    val beskrivelseVerdi: String? = null,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
    JsonSubTypes.Type(HarIkkeVerdierInput::class),
    JsonSubTypes.Type(HarVerdierInput::class),
)
interface VerdierInput

data class HarIkkeVerdierInput(
    val hasBekreftelse: Boolean = false,
) : VerdierInput

data class HarVerdierInput(
    val hasBekreftelse: Boolean = true,
    val hasBolig: Boolean = false,
    val hasCampingvogn: Boolean = false,
    val hasKjoretoy: Boolean = false,
    val hasFritidseiendom: Boolean = false,
    val hasBeskrivelseVerdi: Boolean = false,
    val beskrivelseVerdi: String? = null,
) : VerdierInput

private fun HarVerdierInput.toTypeSet(): Set<FormueType> {
    return setOf(
        if (hasBolig) FormueType.VERDI_BOLIG else null,
        if (hasCampingvogn) FormueType.VERDI_CAMPINGVOGN else null,
        if (hasKjoretoy) FormueType.VERDI_KJORETOY else null,
        if (hasFritidseiendom) FormueType.VERDI_FRITIDSEIENDOM else null,
        if (hasBeskrivelseVerdi) FormueType.VERDI_ANNET else null,
    )
        .filterNotNull().toSet()
}
