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
        return boutgiftService.getBoutgifter(soknadId)?.let {
            if (it.isEmpty()) {
                BoutgifterDto(bekreftelse = false)
            } else {
                it.toBoutgifterDto()
            }
        }
            ?: BoutgifterDto()
    }

    @PutMapping
    fun updateBoutgifter(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) boutgifterInput: BoutgifterInput,
    ): BoutgifterDto {
        return getBoutgifter(soknadId)
    }

    private fun Set<Utgift>.toBoutgifterDto() =
        BoutgifterDto(
            bekreftelse = true,
            husleie = any { it.type == UtgiftType.UTGIFTER_HUSLEIE },
            strom = any { it.type == UtgiftType.UTGIFTER_STROM },
            kommunalAvgift = any { it.type == UtgiftType.UTGIFTER_KOMMUNAL_AVGIFT },
            oppvarming = any { it.type == UtgiftType.UTGIFTER_OPPVARMING },
            boliglan = any { it.type == UtgiftType.UTGIFTER_BOLIGLAN_RENTER || it.type == UtgiftType.UTGIFTER_BOLIGLAN_AVDRAG },
            annet = any { it.type == UtgiftType.UTGIFTER_ANNET_BO },
        )

    private fun skalViseInfoVedBekreftelse(soknadId: UUID): Boolean {
        val skalVise: Boolean = boutgiftService.skalViseInfoVedBekreftelse(soknadId)

        return false
    }
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
    val hasBolig: Boolean = false,
    val hasCampingvogn: Boolean = false,
    val hasKjoretoy: Boolean = false,
    val hasFritidseiendom: Boolean = false,
    val hasBeskrivelseVerdi: Boolean = false,
    val beskrivelseVerdi: String? = null,
) : BoutgifterInput {
    val hasBekreftelse: Boolean = true
}
