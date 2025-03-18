package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.withTimeoutOrNull
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.OPPRETTET
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadJobService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

@Component
class SlettGamleSoknaderJob(
    private val leaderElection: LeaderElection,
    private val soknadJobService: SoknadJobService,
    private val metadataService: SoknadMetadataService,
) {
    @Scheduled(cron = KLOKKEN_TRE_OM_NATTEN)
    suspend fun slettGamleSoknader() {
        runCatching {
            if (leaderElection.isLeader()) {
                withTimeoutOrNull(60.seconds) {
                    val metadataForEksisterendeSoknader =
                        metadataService.findForIdsOlderThan(
                            soknadIds = soknadJobService.getAllSoknader().map { it.id },
                            timestamp = getTimestamp(),
                        )

                    // gamle soknader som skal slettes
                    metadataForEksisterendeSoknader
                        .filter { it.status == OPPRETTET }
                        .also { handleOldStatusOpprettet(it) }
                }
                    ?: logger.error("Kunne ikke slette gamle søknader, tok for lang tid")
            }
        }
            .onFailure { logger.error("Feil ved sletting av gamle søknader", it) }
    }

    private fun handleOldStatusOpprettet(metadatas: List<SoknadMetadata>) {
        if (metadatas.isNotEmpty()) {
            val deleted = soknadJobService.deleteAllByIdCatchError(metadatas.map { it.soknadId })
            logger.info("Slettet $deleted gamle søknader med status OPPRETTET")
        }
    }

    companion object {
        private const val NUMBER_OF_DAYS = 14L
        private const val KLOKKEN_TRE_OM_NATTEN = "0 0 3 * * *"
        private val logger by logger()

        private fun getTimestamp() = LocalDateTime.now().minusDays(NUMBER_OF_DAYS)
    }
}
