package no.nav.sosialhjelp.soknad.v2.scheduled.jobs

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.OPPRETTET
import no.nav.sosialhjelp.soknad.v2.scheduled.AbstractJob
import no.nav.sosialhjelp.soknad.v2.scheduled.LeaderElection
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadJobService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class SlettGamleSoknaderJob(
    leaderElection: LeaderElection,
    private val soknadJobService: SoknadJobService,
    private val metadataService: SoknadMetadataService,
) : AbstractJob(leaderElection, "Slette soknader") {
    // TODO En gang i døgnet
    @Scheduled(cron = "0 */10 * * * * ")
    suspend fun slettGamleSoknader() =
        doInJob {
            val soknadIds = soknadJobService.findSoknadIdsOlderThanWithStatus(getTimestamp(), OPPRETTET)
            if (soknadIds.isNotEmpty()) handleOldSoknadIds(soknadIds)
        }

    private fun handleOldSoknadIds(soknadIds: List<UUID>) {
        var deleted = 0

        soknadIds.forEach { soknadId ->
            runCatching { soknadJobService.deleteSoknadById(soknadId) }
                .onSuccess {
                    deleted++
                    metadataService.deleteMetadata(soknadId)
                }
                .onFailure { logger.error("SletteSoknaderJob -> Kunne ikke slette soknad", it) }
                .getOrNull()
        }
        logger.info("Slettet $deleted gamle søknader med status OPPRETTET")
    }

    companion object {
        private val logger by logger()

        private const val NUMBER_OF_DAYS = 14L

        private fun getTimestamp() = LocalDateTime.now().minusDays(NUMBER_OF_DAYS)
    }
}
