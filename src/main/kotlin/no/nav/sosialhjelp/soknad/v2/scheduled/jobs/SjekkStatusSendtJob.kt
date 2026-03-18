package no.nav.sosialhjelp.soknad.v2.scheduled.jobs

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
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
    private val prometheusMetricsService: PrometheusMetricsService,
) : AbstractJob(leaderElection, "Sjekk status sendt", logger) {
    @Scheduled(cron = "0 0 * * * *")
    fun sjekkStatusSendt() = doInJob { doCheckSoknaderStatusSendt() }

    private fun doCheckSoknaderStatusSendt() {
        metadataService
            .findMetadataForStatus(SoknadStatus.SENDT)
            .filter { it.sentIsOlderThan7Days() }
            .also { sentOlderThan7Days ->
                if (sentOlderThan7Days.isNotEmpty()) {
                    logger.error("Fant ${sentOlderThan7Days.size} søknader med status SENDT eldre enn 7 dager")
                }
                prometheusMetricsService.setAntallGamleSoknaderStatusSendt(sentOlderThan7Days.size)
            }
    }

    private fun SoknadMetadata.sentIsOlderThan7Days(): Boolean =
        tidspunkt.sendtInn?.isBefore(nowWithMillis().minusDays(7)) ?: error("Metadata Mangler 'sendt_inn'")

    companion object {
        private val logger by logger()
    }
}
