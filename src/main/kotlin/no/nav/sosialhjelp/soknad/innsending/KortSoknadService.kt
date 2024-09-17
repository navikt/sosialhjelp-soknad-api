package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class KortSoknadService(
    private val digisosApiService: DigisosApiService,
) {
    fun qualifies(
        token: String?,
        kommunenummer: String,
    ): Boolean = hasRecentSoknadFromFiks(token, kommunenummer) || hasRecentOrUpcomingUtbetalinger(token)

    private fun hasRecentSoknadFromFiks(
        token: String?,
        kommunenummer: String,
    ): Boolean = digisosApiService.qualifiesForKortSoknadThroughSoknader(token, LocalDateTime.now().minusDays(120), kommunenummer)

    private fun hasRecentOrUpcomingUtbetalinger(token: String?): Boolean = digisosApiService.qualifiesForKortSoknadThroughUtbetalinger(token, LocalDateTime.now().minusDays(120), LocalDateTime.now().plusDays(14))
}
