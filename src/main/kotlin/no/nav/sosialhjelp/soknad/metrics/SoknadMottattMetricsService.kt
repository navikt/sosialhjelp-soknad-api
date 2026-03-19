package no.nav.sosialhjelp.soknad.metrics

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataJobService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.scheduled.LeaderElection
import no.nav.sosialhjelp.soknad.v2.scheduled.jobs.SjekkStatusSendtJob.Companion.DAYS
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class SoknadMottattMetricsService(
    metricsManager: MetricsManager,
    private val leaderElection: LeaderElection,
    private val metadataJobService: SoknadMetadataJobService,
) {
    private val antallGamleSoknaderStatusSendtGauge =
        metricsManager.createIntegerGauge(METRIC_NAME, METRIC_DESCRIPTION)

    @EventListener(ApplicationReadyEvent::class)
    fun initializeSoknadMottattGauge() {
        if (leaderElection.isLeader()) doInitialize()
    }

    private fun doInitialize() {
        metadataJobService
            .findMetadataForStatus(SoknadStatus.SENDT)
            .filter { metadata -> metadata.sentIsOlderThan(DAYS) }
            .also { metadatas ->
                logger.info("Initialiserer gauge for ${metadatas.size} antall søknader eldre enn $DAYS dager med status SENDT")
                setAntallGamleSoknaderStatusSendt(metadatas.size)
            }
    }

    fun setAntallGamleSoknaderStatusSendt(antall: Int) {
        antallGamleSoknaderStatusSendtGauge.set(antall)
    }

    companion object {
        private val logger by logger()
        private const val METRIC_NAME = "soknad.old.status.sendt"
        private const val METRIC_DESCRIPTION =
            "Hvis det finnes søknader med status sendt eldre en $DAYS dager bør de sjekkes opp"
    }
}

private fun SoknadMetadata.sentIsOlderThan(days: Long): Boolean =
    tidspunkt.sendtInn?.isBefore(nowWithMillis().minusDays(days))
        ?: error("Metadata Mangler 'sendt_inn'")
