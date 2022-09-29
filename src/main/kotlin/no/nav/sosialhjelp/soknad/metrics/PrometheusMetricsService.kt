package no.nav.sosialhjelp.soknad.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class PrometheusMetricsService(
    private val meterRegistry: MeterRegistry
) {

    private val startSoknadCounter = Counter.builder("start_soknad_counter")

    private val avbruttSoknadCounter = Counter.builder("avbrutt_soknad_counter")

    private val soknadMottakerCounter = Counter.builder("soknad_mottaker_counter")

    private val sendtSoknadSvarUtCounter = Counter.builder("sendt_soknad_svarut_counter")
    private val sendtSoknadDigisosApiCounter = Counter.builder("sendt_soknad_digisosapi_counter")

    private val feiletSendingMedSvarUtCounter = Counter.builder("feilet_sending_svarut_counter")
    private val feiletSendingMedDigisosApiCounter = Counter.builder("feilet_sending_digisosapi_counter")

    private val oppgaverFeilet = AtomicInteger(0)
    private val oppgaverStuckUnderArbeid = AtomicInteger(0)

    private val oppgaverFeiletGauge = Gauge.builder("oppgaver_feilet_gauge", oppgaverFeilet) { it.toDouble() }
        .description("Antall av oppgaver med status FEILET i db")
        .register(meterRegistry)

    private val oppgaverStuckUnderArbeidGauge = Gauge.builder("oppgaver_stuck_underarbeid_gauge", oppgaverStuckUnderArbeid) { it.toDouble() }
        .description("Antall av oppgaver stuck med status UNDER_ARBEID i db")
        .register(meterRegistry)

    fun reportStartSoknad(isEttersendelse: Boolean) {
        startSoknadCounter
            .tag(TAG_ETTERSENDELSE, isEttersendelse.toString())
            .register(meterRegistry)
            .increment()
    }

    fun reportSoknadMottaker(isEttersendelse: Boolean, navEnhet: String) {
        soknadMottakerCounter
            .tag(TAG_ETTERSENDELSE, isEttersendelse.toString())
            .tag(TAG_MOTTAKER, navEnhet)
            .register(meterRegistry)
            .increment()
    }

    fun reportSendtMedSvarUt(isEttersendelse: Boolean) {
        sendtSoknadSvarUtCounter
            .tag(TAG_ETTERSENDELSE, isEttersendelse.toString())
            .register(meterRegistry)
            .increment()
    }

    fun reportSendtMedDigisosApi() {
        sendtSoknadDigisosApiCounter
            .register(meterRegistry)
            .increment()
    }

    fun reportFeiletMedSvarUt(isEttersendelse: Boolean) {
        feiletSendingMedSvarUtCounter
            .tag(TAG_ETTERSENDELSE, isEttersendelse.toString())
            .register(meterRegistry)
            .increment()
    }

    fun reportFeiletMedDigisosApi() {
        feiletSendingMedDigisosApiCounter
            .register(meterRegistry)
            .increment()
    }

    fun reportAvbruttSoknad(isEttersendelse: Boolean) {
        avbruttSoknadCounter
            .tag(TAG_ETTERSENDELSE, isEttersendelse.toString())
            .register(meterRegistry)
            .increment()
    }

    fun resetOppgaverFeiletOgStuckUnderArbeid() {
        oppgaverFeilet.set(0)
        oppgaverStuckUnderArbeid.set(0)
    }

    fun reportOppgaverFeilet(antall: Int) {
        oppgaverFeilet.set(antall)
    }

    fun reportOppgaverStuckUnderArbeid(antall: Int) {
        oppgaverStuckUnderArbeid.set(antall)
    }

    companion object {
        const val TAG_ETTERSENDELSE = "ettersendelse"
        const val TAG_MOTTAKER = "mottaker"
    }
}
