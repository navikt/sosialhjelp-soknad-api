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
) : AbstractJob(leaderElection, "Slette soknader") {
    @Scheduled(cron = KLOKKEN_TRE_OM_NATTEN)
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
                .onFailure { logger.error("Kunne ikke slette soknad", it) }
                .getOrNull()
        }
        logger.info("Slettet $deleted gamle søknader med status OPPRETTET")
    }

    // TODO Fjern når den har kjørt/ryddet opp
    @Deprecated("Fjern når soknader/metadata med status AVBRUTT er fjernet")
    @Scheduled(cron = HVER_TIME)
    suspend fun ryddeOppStatusAvbrutt() =
        doInJob {
            logger.info("Rydder opp søknader med status AVBRUTT.")

            val idsWithStatusAvbrutt = soknadJobService.findSoknadIdsWithStatus(AVBRUTT)
            logger.info("${idsWithStatusAvbrutt.size} søknader/metadata med status AVBRUTT. Sletter.")
            metadataService.deleteAll(idsWithStatusAvbrutt)
            logger.info("Slettet ${idsWithStatusAvbrutt.size} søknader med status AVBRUTT")
        }

    companion object {
        private val logger by logger()

        private const val NUMBER_OF_DAYS = 14L
        private const val KLOKKEN_TRE_OM_NATTEN = "0 0 3 * * *"

        // midlertidig
        private const val HVER_TIME = "0 0 * * * *"

        private fun getTimestamp() = LocalDateTime.now().minusDays(NUMBER_OF_DAYS)
    }
}
