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
            .tag("ettersendelse", isEttersendelse.toString())
            .register(meterRegistry)
            .increment()
    }

    fun reportSendtSoknad(isEttersendelse: Boolean, type: String, navEnhet: String) {
        sendtSoknadCounter
            .tag("ettersendelse", isEttersendelse.toString())
            .tag("type", type)
            .tag("mottaker", navEnhet)
            .register(meterRegistry)
            .increment()
    }

    fun reportFeiletSendingSoknad(isEttersendelse: Boolean, type: String) {
        feiletSendingSoknadCounter
            .tag("ettersendelse", isEttersendelse.toString())
            .tag("type", type)
            .register(meterRegistry)
            .increment()
    }

    fun reportAvbruttSoknad(isEttersendelse: Boolean) {
        avbruttSoknadCounter
            .tag("ettersendelse", isEttersendelse.toString())
            .register(meterRegistry)
            .increment()
    }

    companion object {
        const val SVARUT = "svarut"
        const val DIGISOS_API = "digisos_api"
    }
}
