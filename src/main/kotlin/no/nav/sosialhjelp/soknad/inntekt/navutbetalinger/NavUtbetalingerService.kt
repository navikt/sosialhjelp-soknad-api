package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.NavUtbetaling
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.NavUtbetalingerDto
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.toDomain
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component

@Component
open class NavUtbetalingerService(
    private val navUtbetalingerClient: NavUtbetalingerClient
) {

    open fun getUtbetalingerSiste40Dager(ident: String): List<NavUtbetaling>? {
        val responseDto: NavUtbetalingerDto? = navUtbetalingerClient.getUtbetalingerSiste40Dager(ident)
        if (responseDto == null || responseDto.feilet || responseDto.utbetalinger == null) {
            return null
        }

        val utbetalinger = responseDto.utbetalinger.map { it.toDomain }
        log.info("Antall navytelser utbetaling ${utbetalinger.size}. ${komponenterLogg(utbetalinger)}")
        return utbetalinger
    }

    private fun komponenterLogg(utbetalinger: List<NavUtbetaling>): String {
        if (utbetalinger.isEmpty()) {
            return ""
        }
        return utbetalinger.joinToString(prefix = "Antall komponenter: ", separator = ", ") { "Utbetaling${utbetalinger.indexOf(it)} - ${it.komponenter.size}" }
    }

    companion object {
        private val log = getLogger(NavUtbetalingerService::class.java)
    }
}
