package no.nav.sosialhjelp.soknad.v2.okonomi.utgift

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
@RequestMapping("/soknad/{soknadId}/utgifter/boutgifter", produces = [MediaType.APPLICATION_JSON_VALUE])
class BoutgiftController(
    private val boutgiftService: BoutgiftService,
) {
    @GetMapping
    fun getBoutgifter(
        @PathVariable("soknadId") soknadId: UUID,
    ): BoutgifterDto {
        val skalVise = boutgiftService.skalViseInfoVedBekreftelse(soknadId)

        return boutgiftService.getBoutgifter(soknadId)
            ?.let {
                if (it.isNotEmpty()) {
                    it.toBoutgifterDto(skalVise)
                } else {
                    BoutgifterDto(
                        bekreftelse = false,
                        skalViseInfoVedBekreftelse = skalVise,
                    )
                }
            }
            ?: BoutgifterDto(skalViseInfoVedBekreftelse = skalVise)
    }

    @PutMapping
    fun updateBoutgifter(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) input: BoutgifterInput,
    ): BoutgifterDto {
        when (input) {
            is HarBoutgifterInput -> boutgiftService.updateBoutgifter(soknadId, input.toUtgiftTypeSet())
            else -> boutgiftService.removeBoutgifter(soknadId)
        }
        return getBoutgifter(soknadId)
    }

    private fun skalViseInfoVedBekreftelse(soknadId: UUID): Boolean {
        return boutgiftService.skalViseInfoVedBekreftelse(soknadId)
    }
}

private fun HarBoutgifterInput.toUtgiftTypeSet(): Set<UtgiftType> {
    return setOf(
        if (hasHusleie) UtgiftType.UTGIFTER_HUSLEIE else null,
        if (hasStrom) UtgiftType.UTGIFTER_STROM else null,
        if (hasKommunalAvgift) UtgiftType.UTGIFTER_KOMMUNAL_AVGIFT else null,
        if (hasOppvarming) UtgiftType.UTGIFTER_OPPVARMING else null,
        if (hasBoliglan) UtgiftType.UTGIFTER_BOLIGLAN_AVDRAG else null,
        if (hasBoliglan) UtgiftType.UTGIFTER_BOLIGLAN_RENTER else null,
        if (hasAnnenBoutgift) UtgiftType.UTGIFTER_ANNET_BO else null,
    )
        .filterNotNull().toSet()
}

data class BoutgifterDto(
    val bekreftelse: Boolean? = null,
    val husleie: Boolean = false,
    val strom: Boolean = false,
    val kommunalAvgift: Boolean = false,
    val oppvarming: Boolean = false,
    val boliglan: Boolean = false,
    val annet: Boolean = false,
    val skalViseInfoVedBekreftelse: Boolean = false,
)

private fun Set<Utgift>.toBoutgifterDto(skalViseInfoVedBekreftelse: Boolean) =
    BoutgifterDto(
        bekreftelse = true,
        husleie = any { it.type == UtgiftType.UTGIFTER_HUSLEIE },
        strom = any { it.type == UtgiftType.UTGIFTER_STROM },
        kommunalAvgift = any { it.type == UtgiftType.UTGIFTER_KOMMUNAL_AVGIFT },
        oppvarming = any { it.type == UtgiftType.UTGIFTER_OPPVARMING },
        boliglan = any { it.type == UtgiftType.UTGIFTER_BOLIGLAN_RENTER || it.type == UtgiftType.UTGIFTER_BOLIGLAN_AVDRAG },
        annet = any { it.type == UtgiftType.UTGIFTER_ANNET_BO },
        skalViseInfoVedBekreftelse = skalViseInfoVedBekreftelse,
    )

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
    JsonSubTypes.Type(HarIkkeBoutgifterInput::class),
    JsonSubTypes.Type(HarBoutgifterInput::class),
)
interface BoutgifterInput

class HarIkkeBoutgifterInput : BoutgifterInput {
    val hasBekreftelse: Boolean = false
}

data class HarBoutgifterInput(
    val hasHusleie: Boolean = false,
    val hasStrom: Boolean = false,
    val hasKommunalAvgift: Boolean = false,
    val hasOppvarming: Boolean = false,
    val hasBoliglan: Boolean = false,
    val hasAnnenBoutgift: Boolean = false,
    val beskrivelseAnnenBoutgift: String? = null,
) : BoutgifterInput {
    val hasBekreftelse: Boolean = true
}
