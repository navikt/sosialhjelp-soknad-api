package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.withTimeoutOrNull
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.AVBRUTT
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.OPPRETTET
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadJobService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@Component
class SlettGamleSoknaderJob(
    private val leaderElection: LeaderElection,
    private val soknadJobService: SoknadJobService,
    private val metadataService: SoknadMetadataService,
) {
    @Scheduled(cron = KLOKKEN_TRE_OM_NATTEN)
    suspend fun slettGamleSoknader() {
        // TODO Skal gamle soknader beholde status OPPRETTET etter sletting - eller ha en annen status? (Eller fjernes)
        runCatching {
            if (leaderElection.isLeader()) {
                withTimeoutOrNull(60.seconds) {
                    val soknadIds = soknadJobService.findSoknadIdsOlderThanWithStatus(getTimestamp(), OPPRETTET)
                    if (soknadIds.isNotEmpty()) handleOldSoknadIds(soknadIds)
                }
                    ?: logger.error("Kunne ikke slette gamle søknader, tok for lang tid")
            }
        }
            .onFailure { logger.error("Feil ved sletting av gamle søknader", it) }
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
