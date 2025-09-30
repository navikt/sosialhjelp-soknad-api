package no.nav.sosialhjelp.soknad.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class PrometheusMetricsService(
    private val meterRegistry: MeterRegistry,
) {
    private val startSoknadCounter =
        Counter.builder("start_soknad_counter").also {
            it.register(meterRegistry).increment(0.0)
        }

    private val avbruttSoknadCounter =
        Counter.builder("avbrutt_soknad_counter").also {
            it.register(meterRegistry).increment(0.0)
        }

    private val soknadMottakerCounter =
        Counter.builder("soknad_mottaker_counter").also {
            it.register(meterRegistry).increment(0.0)
        }

    private val sendtSoknadDigisosApiCounter =
        Counter.builder("sendt_soknad_digisosapi_counter").also {
            it.register(meterRegistry).increment(0.0)
        }

    private val feiletSendingMedDigisosApiCounter =
        Counter.builder("feilet_sending_digisosapi_counter").also {
            it.register(meterRegistry).increment(0.0)
        }

    private val feilVedOpprettingAvSoknad =
        Counter.builder("feil_ved_oppretting_avsoknad_counter").also {
            it.register(meterRegistry).increment(0.0)
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

    fun reportSendt(
        kort: Boolean,
    ) {
        sendtSoknadDigisosApiCounter
            .tag("kortSoknad", kort.toString())
            .register(meterRegistry)
            .increment()
    }

    fun reportSendSoknadFeilet() {
        feiletSendingMedDigisosApiCounter
            .register(meterRegistry)
            .increment()
    }

    fun reportStartSoknadFeilet() {
        feilVedOpprettingAvSoknad
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
