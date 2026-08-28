package no.nav.sosialhjelp.soknad.metrics

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.metadata.GAMMEL_SOKNAD_ARBEIDSDAGER
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class SoknadMottattMetricsService(
    metricsManager: MetricsManager,
    private val metadataJobService: SoknadMetadataService,
) {
    private val antallGamleSoknaderStatusSendtGauge =
        metricsManager.createIntegerGauge(METRIC_NAME, METRIC_DESCRIPTION)

    @EventListener(ApplicationReadyEvent::class)
    fun doInitialize() {
        metadataJobService
            .findOldSoknaderStatusSendt()
            .also { metadatas ->
                logger.info("Initialiserer gauge for ${metadatas.size} antall søknader eldre enn $GAMMEL_SOKNAD_ARBEIDSDAGER arbeidsdager med status SENDT")
                antallGamleSoknaderStatusSendtGauge.set(metadatas.size)
            }
    }

    fun setAntallGamleSoknaderStatusSendt(antall: Int) {
        logger.info("Setter verdi for $METRIC_NAME til $antall")
        antallGamleSoknaderStatusSendtGauge.set(antall)
    }

    companion object {
        private val logger by logger()
        private const val METRIC_NAME = "soknad.old.status.sendt"
        private val METRIC_DESCRIPTION =
            "Hvis det finnes søknader med status sendt eldre en $GAMMEL_SOKNAD_ARBEIDSDAGER arbeidsdager bør de sjekkes opp"
    }
}
