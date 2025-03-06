package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.withTimeoutOrNull
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

@Component
class SlettGammelMetadataJob(
    private val leaderElection: LeaderElection,
    // TODO Bruke service fremfor repository direkte pga debug/sporing?
    private val soknadMetadataRepository: SoknadMetadataRepository,
) {
    @Scheduled(cron = KLOKKEN_TRE_OM_NATTEN)
    suspend fun slettGammelMetadata() {
        runCatching {
            if (leaderElection.isLeader()) {
                val result =
                    withTimeoutOrNull(60.seconds) {
                        soknadMetadataRepository
                            .hentEldreEnn(LocalDateTime.now().minusDays(200))
                            .also { metadataUuids ->
                                soknadMetadataRepository.deleteAllById(metadataUuids)
                                logger.info("Slettet ${metadataUuids.size} gamle metadata-innslag")
                            }
                    }
                if (result == null) {
                    logger.error("Kunne ikke slette gamle søknader, tok for lang tid")
                }
            }
        }.onFailure {
            logger.error("Feil ved sletting av gamle metadata-innslag", it)
        }
    }

    companion object {
        private val logger by logger()
        private const val KLOKKEN_TRE_OM_NATTEN = "0 0 3 * * *"
    }
}
