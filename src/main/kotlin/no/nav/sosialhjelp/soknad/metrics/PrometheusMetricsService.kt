package no.nav.sosialhjelp.soknad.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class PrometheusMetricsService(
    private val meterRegistry: MeterRegistry,
) {
    private val startSoknadCounter = createCounter("start_soknad_counter")
    private val feiletSendingMedDigisosApiCounter = createCounter("feilet_sending_med_digisos_api_counter")

    private val avbruttSoknadCounterBuilder = createBuilder("avbrutt_soknad_counter")
    private val soknadMottakerCounterBuilder = createBuilder("soknad_mottaker_counter")
    private val sendtSoknadDigisosApiCounterBuilder = createBuilder("sendt_soknad_digisos_api_counter")

    private val antallGamleSoknaderStatusSendtGauge =
        IntegerGauge(
            meterRegistry,
            "antall_gamle_soknader_status_sendt",
            "Hvis det finnes søknader med status sendt eldre en x antall dager betyr det at de enda ikke er mottatt",
        )

    fun reportStartSoknad() {
        startSoknadCounter.increment()
    }

    fun reportFeilet() {
        feiletSendingMedDigisosApiCounter.increment()
    }

    fun reportStartSoknadFeilet() {
//        feilVedOpprettingAvSoknad.increment()
    }

    fun reportSoknadMottaker(navEnhet: String) {
        soknadMottakerCounterBuilder.increment(TAG_MOTTAKER, navEnhet)
    }

    fun reportSendt(kort: Boolean) {
        sendtSoknadDigisosApiCounterBuilder.increment(TAG_KORT, kort.toString())
    }

    fun reportAvbruttSoknad(referer: String?) {
        val steg: String = referer?.substringAfterLast(delimiter = "/", missingDelimiterValue = "ukjent") ?: "ukjent"
        avbruttSoknadCounterBuilder.increment(TAG_STEG, steg)
    }

    fun setAntallGamleSoknaderStatusSendt(antall: Int) {
        antallGamleSoknaderStatusSendtGauge.set(antall)
    }

    private fun createCounter(name: String): Counter =
        Counter.builder(name)
            .register(meterRegistry)
            .also { it.increment(0.0) }

    private fun createBuilder(name: String): Counter.Builder =
        Counter.builder(name)
            .also { it.register(meterRegistry).increment(0.0) }

    private fun Counter.Builder.increment(
        tagKey: String,
        tagValue: String,
    ) =
        this
            .tag(tagKey, tagValue)
            .register(meterRegistry)
            .increment()

    companion object {
        private const val TAG_MOTTAKER = "mottaker"
        private const val TAG_STEG = "steg"
        private const val TAG_KORT = "kortSoknad"
    }
}

private class IntegerGauge(
    meterRegistry: MeterRegistry,
    name: String,
    description: String?,
) {
    private val numberOfSoknader: AtomicInteger = AtomicInteger(0)

    init {
        Gauge
            .builder(name, numberOfSoknader) { it.get().toDouble() }
            .description(description)
            .register(meterRegistry)
    }

    fun set(value: Int) {
        numberOfSoknader.set(value)
    }
}
