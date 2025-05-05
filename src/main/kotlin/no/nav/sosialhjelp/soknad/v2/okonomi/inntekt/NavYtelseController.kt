package no.nav.sosialhjelp.soknad.v2.okonomi.inntekt

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.UtbetalingMedKomponent
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/inntekt/navytelse", produces = [MediaType.APPLICATION_JSON_VALUE])
class NavYtelseController(private val navYtelseService: NavYtelseService) {
    @GetMapping
    fun getNavYtelse(
        @PathVariable("soknadId") soknadId: UUID,
    ): NavYtelseDto {
        return NavYtelseDto(
            utbetalinger = navYtelseService.getNavYtelse(soknadId)?.toUtbetalingerDto() ?: emptyList(),
            fetchUtbetalingerFeilet = navYtelseService.getIntegrasjonStatusNavYtelse(soknadId),
        )
    }
}

private fun Inntekt.toUtbetalingerDto(): List<NavUtbetalingerDto> {
    return inntektDetaljer.detaljer
        .map { it as UtbetalingMedKomponent }
        .map {
            NavUtbetalingerDto(
                type = type,
                ytelsestype = it.tittel,
                utbetalingsdato = it.utbetaling.utbetalingsdato?.toString(),
                belop = it.utbetaling.belop,
            )
        }
}

data class NavYtelseDto(
    val utbetalinger: List<NavUtbetalingerDto> = emptyList(),
    val fetchUtbetalingerFeilet: Boolean? = null,
)

data class NavUtbetalingerDto(
    val type: InntektType? = null,
    val ytelsestype: String? = null,
    val utbetalingsdato: String? = null,
    val belop: Double? = null,
)
