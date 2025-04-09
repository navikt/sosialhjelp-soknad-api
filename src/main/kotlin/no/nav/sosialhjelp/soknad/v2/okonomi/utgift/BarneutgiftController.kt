package no.nav.sosialhjelp.soknad.v2.okonomi.utgift

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.familie.service.ForsorgerService
import no.nav.sosialhjelp.soknad.v2.okonomi.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType
import no.nav.sosialhjelp.soknad.v2.okonomi.hasType
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
@RequestMapping("/soknad/{soknadId}/utgifter/barneutgifter", produces = [MediaType.APPLICATION_JSON_VALUE])
class BarneutgiftController(
    private val barneutgiftService: BarneutgiftService,
    private val forsorgerService: ForsorgerService,
) {
    @GetMapping
    fun getBarneutgifter(
        @PathVariable("soknadId") soknadId: UUID,
    ): BarneutgifterDto {
        if (!hasForsorgerplikt(soknadId)) return BarneutgifterDto()

        return barneutgiftService.getBarneutgifter(soknadId)?.let {
            if (it.isNotEmpty()) {
                it.toBarneutgifterDto(true)
            } else {
                BarneutgifterDto(
                    hasForsorgerplikt = true,
                    hasBekreftelse = barneutgiftService.getBekreftelse(soknadId),
                )
            }
        } ?: BarneutgifterDto(hasForsorgerplikt = true)
    }

    @PutMapping
    fun updateBarneutgifter(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) input: BarneutgifterInput,
    ): BarneutgifterDto {
        if (!hasForsorgerplikt(soknadId)) return BarneutgifterDto()

        when (input) {
            is HarBarneutgifterInput -> barneutgiftService.updateBarneutgifter(soknadId, input.toUtgiftTypeSet())
            is HarIkkeBarneutgifterInput -> barneutgiftService.removeBarneutgifter(soknadId)
            else -> error("Ukjent barneutgiftInput")
        }
        return getBarneutgifter(soknadId)
    }

    private fun hasForsorgerplikt(soknadId: UUID): Boolean {
        return forsorgerService.findForsorger(soknadId)?.let { it.harForsorgerplikt == true } ?: false
    }
}

data class BarneutgifterDto(
    val hasForsorgerplikt: Boolean = false,
    val hasBekreftelse: Boolean? = null,
    val hasFritidsaktiviteter: Boolean = false,
    val hasBarnehage: Boolean = false,
    val hasSfo: Boolean = false,
    val hasTannregulering: Boolean = false,
    val hasAnnenUtgiftBarn: Boolean = false,
)

private fun Set<Utgift>.toBarneutgifterDto(hasForsorgerplikt: Boolean) =
    BarneutgifterDto(
        hasForsorgerplikt = hasForsorgerplikt,
        hasBekreftelse = true,
        hasBarnehage = hasType(UtgiftType.UTGIFTER_BARNEHAGE),
        hasSfo = hasType(UtgiftType.UTGIFTER_SFO),
        hasFritidsaktiviteter = hasType(UtgiftType.UTGIFTER_BARN_FRITIDSAKTIVITETER),
        hasTannregulering = hasType(UtgiftType.UTGIFTER_BARN_TANNREGULERING),
        hasAnnenUtgiftBarn = hasType(UtgiftType.UTGIFTER_ANNET_BARN),
    )

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(HarIkkeBarneutgifterInput::class, name = "HarIkkeBarneutgifterInput"),
    JsonSubTypes.Type(HarBarneutgifterInput::class, name = "HarBarneutgifterInput"),
)
@Schema(
    discriminatorProperty = "type",
    discriminatorMapping = [
        DiscriminatorMapping(value = "HarIkkeBarneutgifterInput", schema = HarIkkeBarneutgifterInput::class),
        DiscriminatorMapping(value = "HarBarneutgifterInput", schema = HarBarneutgifterInput::class),
    ],
)
interface BarneutgifterInput

class HarIkkeBarneutgifterInput : BarneutgifterInput

data class HarBarneutgifterInput(
    val hasFritidsaktiviteter: Boolean = false,
    val hasBarnehage: Boolean = false,
    val hasSfo: Boolean = false,
    val hasTannregulering: Boolean = false,
    val hasAnnenUtgiftBarn: Boolean = false,
) : BarneutgifterInput

private fun HarBarneutgifterInput.toUtgiftTypeSet(): Set<UtgiftType> {
    return setOf(
        if (hasFritidsaktiviteter) UtgiftType.UTGIFTER_BARN_FRITIDSAKTIVITETER else null,
        if (hasBarnehage) UtgiftType.UTGIFTER_BARNEHAGE else null,
        if (hasSfo) UtgiftType.UTGIFTER_SFO else null,
        if (hasTannregulering) UtgiftType.UTGIFTER_BARN_TANNREGULERING else null,
        if (hasAnnenUtgiftBarn) UtgiftType.UTGIFTER_ANNET_BARN else null,
    )
        .filterNotNull().toSet()
}
