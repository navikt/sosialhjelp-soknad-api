package no.nav.sosialhjelp.soknad.metrics

import io.micrometer.core.instrument.Tag
import org.springframework.stereotype.Service

@Service
class SoknadLifecycleMetricsService(prometheusMetricsService: PrometheusMetricsService,) {

    private val startSoknadCounter = prometheusMetricsService.createCounter("start_soknad_counter")
    private val feiletSendingMedDigisosApiCounter = prometheusMetricsService.createCounter("feilet_sending_med_digisos_api_counter")
    private val feilVedOpprettingAvSoknad = prometheusMetricsService.createCounter("feil_ved_oppretting_av_soknad_counter")

    private val kortSoknadSentCounter =
        prometheusMetricsService.createCounter("soknad_sent_counter", Tag.of(TAG_KORT, "true"))
    private val standardSoknadSentCounter =
        prometheusMetricsService.createCounter("soknad_sent_counter", Tag.of(TAG_KORT, "false"))

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
