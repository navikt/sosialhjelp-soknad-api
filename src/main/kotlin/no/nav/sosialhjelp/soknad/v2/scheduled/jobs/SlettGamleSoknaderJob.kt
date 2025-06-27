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
) : AbstractJob(leaderElection, "Slette gamle soknader", logger) {
    @Scheduled(cron = "0 30 3 * * * ")
    suspend fun slettGamleSoknader() = doInJob { findAndDeleteOldSoknader() }

    private fun findAndDeleteOldSoknader() {
        val soknadIds = soknadJobService.findSoknadIdsOlderThanWithStatus(getTimestamp(), OPPRETTET)
        logger.info("Fant ${soknadIds.size} søknader med status OPPRETTET eldre enn $NUMBER_OF_DAYS dager")

        if (soknadIds.isNotEmpty()) {
            handleOldSoknadIds(soknadIds)
        }
    }

    private fun handleOldSoknadIds(soknadIds: List<UUID>) {
        soknadIds.chunked(500).forEach { batch ->
            soknadJobService.deleteSoknaderByIds(batch)
            metadataService.deleteAll(batch)
        }

        logger.info("Slettet ${soknadIds.size} gamle søknader med status OPPRETTET")
    }

    companion object {
        private val logger by logger()

        private const val NUMBER_OF_DAYS = 14L

        private fun getTimestamp() = LocalDateTime.now().minusDays(NUMBER_OF_DAYS)
    }
}
