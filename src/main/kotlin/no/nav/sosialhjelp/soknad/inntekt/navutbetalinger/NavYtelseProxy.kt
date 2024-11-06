package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.NavUtbetalingerDto
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.NavYtelseController
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.NavYtelseDto
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class NavYtelseProxy(private val navYtelseController: NavYtelseController) {
    fun getNavYtelse(soknadId: String): SysteminntekterFrontend {
        return navYtelseController
            .getNavYtelse(UUID.fromString(soknadId))
            .toSysteminntektFrontend()
    }
}

private fun NavYtelseDto.toSysteminntektFrontend(): SysteminntekterFrontend {
    return SysteminntekterFrontend(
        utbetalingerFraNavFeilet = fetchUtbetalingerFeilet,
        systeminntekter = utbetalinger.map { it.toSystemInntektFrontend() },
    )
}

private fun NavUtbetalingerDto.toSystemInntektFrontend() =
    SysteminntektFrontend(
        inntektType = type?.toString(),
        utbetalingsdato = utbetalingsdato,
        belop = belop,
    )
