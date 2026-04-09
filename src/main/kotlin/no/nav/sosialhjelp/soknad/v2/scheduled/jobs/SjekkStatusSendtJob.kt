package no.nav.sosialhjelp.soknad.v2.scheduled.jobs

import no.nav.sosialhjelp.soknad.metrics.SoknadMottattMetricsService
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SjekkStatusSendtJob(
    private val metadataJobService: SoknadMetadataService,
    private val metricsService: SoknadMottattMetricsService,
) {
    @Scheduled(cron = "0 0 */4 * * *")
    fun doCheckSoknaderStatusSendt() {
        metadataJobService
            .findMetadataForStatus(SoknadStatus.SENDT)
            .filter { it.sentIsOlderThan(DAYS) }
            .also { sentOlderThan7Days -> metricsService.setAntallGamleSoknaderStatusSendt(sentOlderThan7Days.size) }
    }

    companion object {
        const val DAYS = 7L
    }
}

private fun SoknadMetadata.sentIsOlderThan(days: Long): Boolean =
    tidspunkt.sendtInn?.isBefore(nowWithMillis().minusDays(days)) ?: error("Metadata Mangler 'sendt_inn'")
