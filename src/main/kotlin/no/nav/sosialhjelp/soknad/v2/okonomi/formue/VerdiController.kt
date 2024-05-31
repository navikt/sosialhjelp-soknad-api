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
        return verdiService.getBekreftelse(soknadId)
            ?.let {
                if (it.verdi) {
                    verdiService.getVerdier(soknadId).toVerdierDto(
                        hasBekreftelse = true,
                        beskrivelseVerdi = verdiService.getBeskrivelseVerdi(soknadId),
                    )
                } else {
                    VerdierDto(bekreftelse = false)
                }
            } ?: VerdierDto()
    }

    @PutMapping
    fun updateVerdier(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) verdierInput: VerdierInput,
    ): VerdierDto {
        when (verdierInput) {
            is HarVerdierInput -> verdiService.updateVerdier(soknadId, verdierInput)
            else -> verdiService.removeVerdier(soknadId)
        }
        return getVerdier(soknadId = soknadId)
    }
}

private fun List<Formue>.toVerdierDto(
    hasBekreftelse: Boolean,
    beskrivelseVerdi: String?,
): VerdierDto {
    return VerdierDto(
        bekreftelse = hasBekreftelse,
        hasBolig = any { it.type == FormueType.VERDI_BOLIG },
        hasCampingvogn = any { it.type == FormueType.VERDI_CAMPINGVOGN },
        hasKjoretoy = any { it.type == FormueType.VERDI_KJORETOY },
        hasFritidseiendom = any { it.type == FormueType.VERDI_FRITIDSEIENDOM },
        hasAnnet = any { it.type == FormueType.VERDI_ANNET },
        beskrivelseVerdi = beskrivelseVerdi,
    )
}

data class VerdierDto(
    val bekreftelse: Boolean? = null,
    val hasBolig: Boolean = false,
    val hasCampingvogn: Boolean = false,
    val hasKjoretoy: Boolean = false,
    val hasFritidseiendom: Boolean = false,
    val hasAnnet: Boolean = false,
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
    val hasBolig: Boolean = false,
    val hasCampingvogn: Boolean = false,
    val hasKjoretoy: Boolean = false,
    val hasFritidseiendom: Boolean = false,
    val beskrivelseVerdi: String? = null,
) : VerdierInput
