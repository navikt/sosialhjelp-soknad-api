package no.nav.sosialhjelp.soknad.metrics

import io.micrometer.core.instrument.Tag
import org.springframework.stereotype.Service

@Service
class SoknadLifecycleMetricsService(
    private val prometheusMetricsService: PrometheusMetricsService,
) {
    private val startSoknadCounter = prometheusMetricsService.createCounter("start_soknad_counter")
    private val feiletSendingMedDigisosApiCounter = prometheusMetricsService.createCounter("feilet_sending_med_digisos_api_counter")
    private val feilVedOpprettingAvSoknad = prometheusMetricsService.createCounter("feil_ved_oppretting_av_soknad_counter")

    // TODO Er disse to metrikkene nødvendige?
    private val kortSoknadSentCounter =
        prometheusMetricsService.createCounterWithTags("soknad_sent_counter", Tag.of(TAG_KORT, "true"))
    private val standardSoknadSentCounter =
        prometheusMetricsService.createCounterWithTags("soknad_sent_counter", Tag.of(TAG_KORT, "false"))

    // TODO Er dette egentlig en metrikk?
    private val avbruttSoknadCounterName = "avbrutt_soknad_counter"

    // TODO Er dette egentlig en metrikk?
    private val soknadMottakerCounterName = "soknad_mottaker_counter"

    fun reportStartSoknad() {
        startSoknadCounter.increment()
    }

    fun reportSendSoknadFeilet() {
        feiletSendingMedDigisosApiCounter.increment()
    }

    fun reportStartSoknadFeilet() {
        feilVedOpprettingAvSoknad.increment()
    }

    fun reportSoknadMottaker(navEnhet: String) {
        prometheusMetricsService.increment(soknadMottakerCounterName, Tag.of(TAG_MOTTAKER, navEnhet))
    }

    fun reportSendt(kort: Boolean) {
        when (kort) {
            true -> kortSoknadSentCounter.increment()
            false -> standardSoknadSentCounter.increment()
        }
    }

    fun reportAvbruttSoknad(referer: String?) {
        val steg: String = referer?.substringAfterLast(delimiter = "/", missingDelimiterValue = "ukjent") ?: "ukjent"
        prometheusMetricsService.increment(avbruttSoknadCounterName, Tag.of(TAG_STEG, steg))
    }

    companion object {
        private const val TAG_MOTTAKER = "mottaker"
        private const val TAG_STEG = "steg"
        private const val TAG_KORT = "kortSoknad"
    }
}
