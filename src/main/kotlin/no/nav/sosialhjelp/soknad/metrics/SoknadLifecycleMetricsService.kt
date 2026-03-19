package no.nav.sosialhjelp.soknad.metrics

import io.micrometer.core.instrument.Tag
import org.springframework.stereotype.Service

@Service
class SoknadLifecycleMetricsService(metricsManager: MetricsManager) {
    private val startSoknadCounter = metricsManager.createCounter("soknad.counter.start")
    private val feiletSendingMedDigisosApiCounter = metricsManager.createCounter("soknad.counter.sending.failed")
    private val feilVedOpprettingAvSoknad = metricsManager.createCounter("soknad.counter.creation.failed")

    private val kortSoknadSentCounter =
        metricsManager.createCounter("soknad.counter.sent", Tag.of(TAG_KORT, "true"))
    private val standardSoknadSentCounter =
        metricsManager.createCounter("soknad.counter.sent", Tag.of(TAG_KORT, "false"))

    fun reportStartSoknad() {
        startSoknadCounter.increment()
    }

    fun reportSendSoknadFeilet() {
        feiletSendingMedDigisosApiCounter.increment()
    }

    fun reportStartSoknadFeilet() {
        feilVedOpprettingAvSoknad.increment()
    }

    fun reportSendt(kort: Boolean) {
        when (kort) {
            true -> kortSoknadSentCounter.increment()
            false -> standardSoknadSentCounter.increment()
        }
    }

    companion object {
        private const val TAG_KORT = "kortSoknad"
    }
}
