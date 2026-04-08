package no.nav.sosialhjelp.soknad.v2.scheduled.jobs

import no.nav.sosialhjelp.soknad.metrics.SoknadMottattMetricsService
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataJobService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDateTime

@Component
class SjekkStatusSendtJob(
    private val metadataJobService: SoknadMetadataJobService,
    private val metricsService: SoknadMottattMetricsService,
) {
    @Scheduled(cron = "0 0 */4 * * MON-FRI")
    fun doCheckSoknaderStatusSendt() {
        metadataJobService
            .findMetadataForStatus(SoknadStatus.SENDT)
            .filter { it.sentIsOlderThan(DAYS) }
            .also { sentOlderThan7Days -> metricsService.setAntallGamleSoknaderStatusSendt(sentOlderThan7Days.size) }
    }

    companion object {
        const val DAYS = 2L
    }
}

private fun LocalDateTime.subtractWorkingDays(days: Long): LocalDateTime {
    var result = this
    var remaining = days
    while (remaining > 0) {
        result = result.minusDays(1)
        if (result.dayOfWeek != DayOfWeek.SATURDAY && result.dayOfWeek != DayOfWeek.SUNDAY) {
            remaining--
        }
    }
    return result
}

private fun SoknadMetadata.sentIsOlderThan(days: Long): Boolean =
    tidspunkt.sendtInn?.isBefore(nowWithMillis().subtractWorkingDays(days)) ?: error("Metadata Mangler 'sendt_inn'")

