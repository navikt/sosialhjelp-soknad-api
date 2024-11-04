package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.withTimeoutOrNull
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@Component
class SlettGamleSoknaderJob(
    private val leaderElection: LeaderElection,
    private val soknadRepository: SoknadRepository,
    private val mellomlagringService: MellomlagringService,
) {
    @Scheduled(cron = KLOKKEN_TRE_OM_NATTEN)
    suspend fun slettGamleSoknader() {
        runCatching {
            if (leaderElection.isLeader()) {
                val result =
                    withTimeoutOrNull(60.seconds) {
                        soknadRepository
                            .findOlderThan(LocalDateTime.now().minusDays(14))
                            .also { oldUuids ->
                                soknadRepository.deleteAllById(oldUuids)
                                slettFilerForSoknader(oldUuids)
                            }
                            .also { oldUuids -> logger.info("Slettet ${oldUuids.size} gamle søknader") }
                    }
                if (result == null) {
                    logger.error("Kunne ikke slette gamle søknader, tok for lang tid")
                }
            }
        }.onFailure {
            logger.error("Feil ved sletting av gamle søknader", it)
        }
    }

    private fun slettFilerForSoknader(oldUuids: List<UUID>) {
        oldUuids.forEach { uuid ->
            runCatching { mellomlagringService.deleteAll(uuid) }
                .onFailure { ex ->
                    logger.warn(
                        "Slette gamle soknaer: Feil eller fantes ingen filer hos FIKS for: $uuid",
                        ex,
                    )
                }
        }
    }

    companion object {
        private const val KLOKKEN_TRE_OM_NATTEN = "0 0 3 * * *"
        private val logger by logger()
    }
}
