package no.nav.sosialhjelp.soknad.v2.scheduled.jobs

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.AVBRUTT
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
) : AbstractJob(jobName = "Sletter gamle soknader som ikke er sendt inn", leaderElection) {
    @Scheduled(cron = KLOKKEN_TRE_OM_NATTEN)
    suspend fun slettGamleSoknader() =
        doInJob {
            soknadJobService.findSoknadIdsOlderThanWithStatus(getTimestamp(), OPPRETTET)
                .also { ids -> if (ids.isNotEmpty()) handleOldSoknadIds(ids) }
        }

    private fun handleOldSoknadIds(soknadIds: List<UUID>) {
        var deleted = 0

        soknadIds.forEach { soknadId ->
            runCatching { soknadJobService.deleteSoknadById(soknadId) }
                .onSuccess {
                    deleted++
                    metadataService.updateSoknadStatus(soknadId, AVBRUTT)
                }
                .onFailure { logger.error("Kunne ikke slette soknad", it) }
                .getOrNull()
        }
        logger.info("Slettet $deleted gamle søknader med status OPPRETTET")
    }

    companion object {
        private const val NUMBER_OF_DAYS = 14L
        private const val KLOKKEN_TRE_OM_NATTEN = "0 0 3 * * *"
        private val logger by logger()

        private fun getTimestamp() = LocalDateTime.now().minusDays(NUMBER_OF_DAYS)
    }
}
