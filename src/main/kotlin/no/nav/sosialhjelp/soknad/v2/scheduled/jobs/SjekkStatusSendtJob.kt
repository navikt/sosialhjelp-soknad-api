package no.nav.sosialhjelp.soknad.v2.scheduled.jobs

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.metrics.SoknadMottattMetricsService
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.scheduled.AbstractJob
import no.nav.sosialhjelp.soknad.v2.scheduled.LeaderElection
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SjekkStatusSendtJob(
    leaderElection: LeaderElection,
    private val metadataService: SoknadMetadataService,
    private val metricsService: SoknadMottattMetricsService,
) : AbstractJob(leaderElection, "Sjekk status sendt", logger) {
    @Scheduled(cron = "0 0 */4 * * *")
    fun sjekkStatusSendt() = doInJob { doCheckSoknaderStatusSendt() }

    private fun doCheckSoknaderStatusSendt() {
        metadataService
            .findMetadataForStatus(SoknadStatus.SENDT)
            .filter { it.sentIsOlderThan(DAYS) }
            .also { sentOlderThan7Days ->
                if (sentOlderThan7Days.isNotEmpty()) {
                    logger.error("Fant ${sentOlderThan7Days.size} søknader med status SENDT eldre enn ${DAYS} dager")
                }
                metricsService.setAntallGamleSoknaderStatusSendt(sentOlderThan7Days.size)
            }
    }

    companion object {
        private val logger by logger()
        const val DAYS = 7L
    }
}

private fun SoknadMetadata.sentIsOlderThan(days: Long): Boolean =
    tidspunkt.sendtInn?.isBefore(nowWithMillis().minusDays(days)) ?: error("Metadata Mangler 'sendt_inn'")
