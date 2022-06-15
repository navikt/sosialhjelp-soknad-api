package no.nav.sosialhjelp.soknad.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class PrometheusMetricsService(
    private val meterRegistry: MeterRegistry
) {

    private val startSoknadCounter = Counter.builder("start_soknad_counter")
    private val avbruttSoknadCounter = Counter.builder("avbrutt_soknad_counter")
    private val sendtSoknadCounter = Counter.builder("sendt_soknad_counter")
    private val feiletSendingSoknadCounter = Counter.builder("feilet_sending_soknad_counter")

    fun reportStartSoknad(isEttersendelse: Boolean) {
        startSoknadCounter
            .tag(TAG_ETTERSENDELSE, isEttersendelse.toString())
            .register(meterRegistry)
            .increment()
    }

    fun reportSendtSoknad(isEttersendelse: Boolean, type: String, navEnhet: String) {
        sendtSoknadCounter
            .tag(TAG_ETTERSENDELSE, isEttersendelse.toString())
            .tag(TAG_TYPE_API, type)
            .tag(TAG_MOTTAKER, navEnhet)
            .register(meterRegistry)
            .increment()
    }

    fun reportFeiletSendingSoknad(isEttersendelse: Boolean, type: String) {
        feiletSendingSoknadCounter
            .tag(TAG_ETTERSENDELSE, isEttersendelse.toString())
            .tag(TAG_TYPE_API, type)
            .register(meterRegistry)
            .increment()
    }

    fun reportAvbruttSoknad(isEttersendelse: Boolean) {
        avbruttSoknadCounter
            .tag(TAG_ETTERSENDELSE, isEttersendelse.toString())
            .register(meterRegistry)
            .increment()
    }

    companion object {
        const val SVARUT = "svarut"
        const val DIGISOS_API = "digisos_api"

        const val TAG_ETTERSENDELSE = "ettersendelse"
        const val TAG_TYPE_API = "type"
        const val TAG_MOTTAKER = "mottaker"
    }
}
