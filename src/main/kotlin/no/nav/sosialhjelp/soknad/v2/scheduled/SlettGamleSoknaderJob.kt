package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.withTimeoutOrNull
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

const val KLOKKEN_TRE_OM_NATTEN = "0 0 3 * * *"

@Component
class SlettGamleSoknaderJob(
    private val leaderElection: LeaderElection,
    private val soknadRepository: SoknadRepository,
) {
    private val log by logger()

    @Scheduled(cron = KLOKKEN_TRE_OM_NATTEN)
    suspend fun slettGamleSoknader() =
        kotlin.runCatching {
            if (leaderElection.isLeader()) {
                val result =
                    withTimeoutOrNull(60.seconds) {
                        val olderThanTwoWeeks = soknadRepository.findOlderThan(LocalDateTime.now().minusDays(14))
                        // TODO: Slett også vedlegg når det er implementert
                        olderThanTwoWeeks.onEach {
                            soknadRepository.delete(it)
                        }.also {
                            log.info("Slettet ${it.size} gamle søknader")
                        }
                    }
                if (result == null) {
                    log.warn("Kunne ikke slette gamle søknader, tok for lang tid")
                }
            }
        }.onFailure {
            log.error("Feil ved sletting av gamle søknader", it)
        }
}
