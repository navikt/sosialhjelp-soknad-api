package no.nav.sosialhjelp.soknad.v2.scheduled.jobs

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.scheduled.AbstractJob
import no.nav.sosialhjelp.soknad.v2.scheduled.LeaderElection
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadJobService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SjekkStatusMottattJob(
    leaderElection: LeaderElection,
    private val soknadJobService: SoknadJobService,
) : AbstractJob(leaderElection, "Sjekke status MOTTATT_FSL", logger) {
    @Scheduled(cron = "0 30 */2 * * *")
    fun sjekkStatusMottatt() = doInJob { doSjekkStatusMottatt() }

    private fun doSjekkStatusMottatt() {
        val existingSoknadIds =
            soknadJobService.findSoknadIdsWithStatus(SoknadStatus.MOTTATT_FSL)

        if (existingSoknadIds.isNotEmpty()) {
            logger.error("Fant ${existingSoknadIds.size} søknader med status MOTTATT_FSL. Sletter.")
            soknadJobService.deleteSoknaderByIds(existingSoknadIds)
        }
    }

    companion object {
        private val logger by logger()
    }
}
