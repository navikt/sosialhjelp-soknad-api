package no.nav.sosialhjelp.soknad.v2.scheduled.jobs

import no.nav.sosialhjelp.soknad.metrics.SoknadMottattMetricsService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SjekkStatusSendtJob(
    private val metadataJobService: SoknadMetadataService,
    private val metricsService: SoknadMottattMetricsService,
) {
    @Scheduled(cron = "0 0 */4 * * MON-FRI")
    fun doCheckSoknaderStatusSendt() {
        metadataJobService
            .findOldSoknaderStatusSendt()
            .also { gamleSoknader -> metricsService.setAntallGamleSoknaderStatusSendt(gamleSoknader.size) }
    }
}
