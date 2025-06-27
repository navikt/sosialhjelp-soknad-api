package no.nav.sosialhjelp.soknad.v2.scheduled.jobs

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.scheduled.AbstractJob
import no.nav.sosialhjelp.soknad.v2.scheduled.LeaderElection
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadJobService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class SletteSoknaderStatusFeiletJob(
    leaderElection: LeaderElection,
    private val soknadJobService: SoknadJobService,
) : AbstractJob(leaderElection, "Slette soknader med status INNSENDING_FEILET", logger) {
    @Scheduled(cron = "0 0 4 * * *")
    suspend fun sletteSoknaderStatusFeilet() = doInJob { findAndDeleteSoknaderStatusFeilet() }

    private fun findAndDeleteSoknaderStatusFeilet() {
        soknadJobService.findSoknadIdsOlderThanWithStatus(getTimeStamp(), SoknadStatus.INNSENDING_FEILET)
            .also { if (it.isNotEmpty()) handleSoknaderInnsendingFeilet(it) }
    }

    private fun handleSoknaderInnsendingFeilet(soknadIds: List<UUID>) {
        logger.info("Sletter ${soknadIds.size} soknader hvor innsending feilet.")
        soknadIds.forEach { id ->
            runCatching { soknadJobService.deleteSoknadById(id) }
                .onFailure { logger.error("Feil ved sletting av gammel soknad $id med status INNSENDING_FEILET", it) }
                .getOrNull()
        }
    }

    private fun getTimeStamp(): LocalDateTime = nowWithMillis().minusDays(NUMBER_OF_DAYS + EXTRA_DAYS)

    companion object {
        private val logger by logger()
        private const val NUMBER_OF_DAYS = 14L
        private const val EXTRA_DAYS = 5L
    }
}
