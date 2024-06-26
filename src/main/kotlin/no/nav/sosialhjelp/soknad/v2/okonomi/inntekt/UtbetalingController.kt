package no.nav.sosialhjelp.soknad.v2.okonomi.inntekt

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
@RequestMapping("/soknad/{soknadId}/inntekt/utbetalinger", produces = [MediaType.APPLICATION_JSON_VALUE])
class UtbetalingController(
    private val utbetalingService: UtbetalingerService,
) {
    @GetMapping
    fun getUtbetalinger(
        @PathVariable("soknadId") soknadId: UUID,
    ): UtbetalingerDto {
        return utbetalingService.getUtbetalinger(soknadId)?.let { utbetalinger ->
            if (utbetalinger.isEmpty()) {
                UtbetalingerDto(hasBekreftelse = false)
            } else {
                utbetalinger.toDto(hasBekreftelse = true)
            }
        }
            ?: UtbetalingerDto()
    }

    @PutMapping
    fun updateUtbetalinger(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody input: UtbetalingerInput,
    ): UtbetalingerDto {
        when (input) {
            is HarUtbetalingerInput ->
                utbetalingService.updateUtbetalinger(
                    soknadId = soknadId,
                    eksisterendeTyper = input.toTypeSet(),
                    beskrivelseAnnet = if (input.hasAnnet) input.beskrivelseUtbetaling else null,
                )
            else -> utbetalingService.removeUtbetalinger(soknadId)
        }
        return getUtbetalinger(soknadId)
    }
}

private fun HarUtbetalingerInput.toTypeSet(): Set<InntektType> {
    return setOf(
        if (hasUtbytte) InntektType.UTBETALING_UTBYTTE else null,
        if (hasSalg) InntektType.UTBETALING_SALG else null,
        if (hasForsikring) InntektType.UTBETALING_FORSIKRING else null,
        if (hasAnnet) InntektType.UTBETALING_ANNET else null,
    )
        .filterNotNull().toSet()
}

data class UtbetalingerDto(
    val hasBekreftelse: Boolean? = null,
    val hasUtbytte: Boolean = false,
    val hasSalg: Boolean = false,
    val hasForsikring: Boolean = false,
    val hasAnnenUtbetaling: Boolean = false,
    val beskrivelseUtbetaling: String? = null,
    // TODO Skal / trenger denne være en del av denne dto'en ?
    val utbetalingerFraNavFeilet: Boolean? = null,
)

private fun List<Inntekt>.toDto(hasBekreftelse: Boolean?) =
    UtbetalingerDto(
        hasBekreftelse = hasBekreftelse,
        hasUtbytte = any { it.type == InntektType.UTBETALING_UTBYTTE },
        hasSalg = any { it.type == InntektType.UTBETALING_SALG },
        hasForsikring = any { it.type == InntektType.UTBETALING_FORSIKRING },
        hasAnnenUtbetaling = any { it.type == InntektType.UTBETALING_ANNET },
        beskrivelseUtbetaling = find { it.type == InntektType.UTBETALING_ANNET }?.beskrivelse,
    )

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
    JsonSubTypes.Type(HarIkkeUtbetalingerInput::class),
    JsonSubTypes.Type(HarUtbetalingerInput::class),
)
interface UtbetalingerInput

data class HarIkkeUtbetalingerInput(
    val bekreftelse: Boolean,
) : UtbetalingerInput

data class HarUtbetalingerInput(
    val hasUtbytte: Boolean,
    val hasSalg: Boolean,
    val hasForsikring: Boolean,
    val hasAnnet: Boolean,
    val beskrivelseUtbetaling: String?,
) : UtbetalingerInput
