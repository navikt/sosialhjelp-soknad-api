package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime

@Component
class QualifiesForKort(
    private val digisosApiService: DigisosApiService,
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val clock: Clock,
) {
    fun qualifies(
        fnr: String,
        token: String?,
    ): Boolean = hasRecentSoknadFromMetadata(fnr) || hasRecentSoknadFromFiks(token) || hasRecentOrUpcomingUtbetalinger(token)

    private fun hasRecentSoknadFromMetadata(fnr: String): Boolean =
        soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(fnr, LocalDateTime.now(clock).minusDays(120)).any()

    private fun hasRecentSoknadFromFiks(token: String?): Boolean = digisosApiService.qualifiesForKortSoknadThroughSoknader(token, LocalDateTime.now().minusDays(120))

    private fun hasRecentOrUpcomingUtbetalinger(token: String?): Boolean = digisosApiService.qualifiesForKortSoknadThroughUtbetalinger(token, LocalDateTime.now().minusDays(120), LocalDateTime.now().plusDays(14))
}
