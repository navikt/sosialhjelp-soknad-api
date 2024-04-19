package no.nav.sosialhjelp.soknad.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

// TODO Hvor hører oppdateringer til prometheus hjemme? Controller, Business-logikk (service) eller...
// TODO ...er det egentlig en side-tjeneste. Kunne vært interessant å skille ut Prometheus-logikk som en egen..
// TODO... interceptor.
@Component
class PrometheusMetricsService(
    private val meterRegistry: MeterRegistry,
) {
    private val startSoknadCounter = Counter.builder("start_soknad_counter")

    private val avbruttSoknadCounter = Counter.builder("avbrutt_soknad_counter")

    private val soknadMottakerCounter = Counter.builder("soknad_mottaker_counter")

    private val sendtSoknadDigisosApiCounter = Counter.builder("sendt_soknad_digisosapi_counter")

    private val feiletSendingMedDigisosApiCounter = Counter.builder("feilet_sending_digisosapi_counter")

    private val soknadInnsendingTidTimer = Timer.builder("soknad_innsending_tid")

    fun reportInnsendingTid(antallSekunder: Long) {
        soknadInnsendingTidTimer
            .register(meterRegistry)
            .record(antallSekunder, TimeUnit.SECONDS)
    }

    fun reportStartSoknad() {
        startSoknadCounter
            .register(meterRegistry)
            .increment()
    }

    fun reportSoknadMottaker(navEnhet: String) {
        soknadMottakerCounter
            .tag(TAG_MOTTAKER, navEnhet)
            .register(meterRegistry)
            .increment()
    }

    fun reportSendt() {
        sendtSoknadDigisosApiCounter
            .register(meterRegistry)
            .increment()
    }

    fun reportFeilet() {
        feiletSendingMedDigisosApiCounter
            .register(meterRegistry)
            .increment()
    }

    fun reportAvbruttSoknad(referer: String?) {
        val steg: String = referer?.substringAfterLast(delimiter = "/", missingDelimiterValue = "ukjent") ?: "ukjent"
        avbruttSoknadCounter
            .tag(TAG_STEG, steg)
            .register(meterRegistry)
            .increment()
    }

    companion object {
        const val TAG_MOTTAKER = "mottaker"
        const val TAG_STEG = "steg"
    }
}
