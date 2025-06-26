package no.nav.sosialhjelp.soknad.v2.scheduled.jobs

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.scheduled.AbstractJob
import no.nav.sosialhjelp.soknad.v2.scheduled.LeaderElection
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadJobService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * Midlertidig job for å slette soknader med status SENDT (= ikke kvittert ut).
 * Midlertidig fordi KS skal tilby ny logikk hvor vi med sikkerhet vet om en søknad er mottatt eller ei
 */
@Component
class SlettGamleSoknaderMedStatusSendtJob(
    leaderElection: LeaderElection,
    private val soknadJobService: SoknadJobService,
) : AbstractJob(jobName = "Slette soknader sendt", leaderElection = leaderElection, logger = logger) {
    // TODO En gang i døgnet
    @Scheduled(cron = "0 0 * * * *")
    suspend fun slettSoknader() =
        doInJob {
            runCatching {
                soknadJobService.findSoknadIdsOlderThanWithStatus(getTimestamp(), SoknadStatus.SENDT)
                    .also { ids -> soknadJobService.deleteSoknaderByIds(ids) }
            }
                .onSuccess { ids -> logger.info("Slettet ${ids.size} soknader med status SENDT") }
                .onFailure { ex -> logger.error("Kunne ikke slette soknader med status SENDT", ex) }
                .getOrThrow()
        }

    companion object {
        private const val NUMBER_OF_DAYS = 7L
        private val logger by logger()

        private fun getTimestamp() = LocalDateTime.now().minusDays(NUMBER_OF_DAYS)
    }
}
