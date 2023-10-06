package no.nav.sosialhjelp.soknad.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
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

    private val soknadInnsendingTidTimer = Timer.builder("soknad_innsending_tid")

    private val soknadLockLatencyTimer =
        Timer.builder("soknad_lock_acquire_latency_ms")
            .description("Average latency for successful attempts to acquire lock")
            .register(meterRegistry)

    private val soknadLockHoldTimer = Timer.builder("soknad_lock_hold_duration_ms")
        .description("Average time between a lock being held and released")
        .register(meterRegistry)

    private val soknadLockAcquiredCount = Counter.builder("soknad_lock_acquired_count")
        .description("Number of successful attempts to acquire a lock")
        .register(meterRegistry)

    private val soknadLockTimeoutCount = Counter.builder("soknad_lock_timeout_count")
        .description("Number of unsuccessful attempts to acquire a lock")
        .register(meterRegistry)

    init {
        Gauge.builder("oppgaver_feilet_gauge", oppgaverFeilet) { it.toDouble() }
            .description("Antall av oppgaver med status FEILET i db")
            .register(meterRegistry)

        Gauge.builder("oppgaver_stuck_underarbeid_gauge", oppgaverStuckUnderArbeid) { it.toDouble() }
            .description("Antall av oppgaver stuck med status UNDER_ARBEID i db")
            .register(meterRegistry)
    }

    fun reportInnsendingTid(antallSekunder: Long) {
        soknadInnsendingTidTimer
            .register(meterRegistry)
            .record(antallSekunder, TimeUnit.SECONDS)
    }

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

    fun reportAvbruttSoknad(isEttersendelse: Boolean, steg: String) {
        avbruttSoknadCounter
            .tag(TAG_ETTERSENDELSE, isEttersendelse.toString())
            .tag(TAG_STEG, steg)
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

    fun reportLockAcquireLatency(lockTimeMs: Long) {
        soknadLockLatencyTimer.record(lockTimeMs, TimeUnit.MILLISECONDS)
        soknadLockAcquiredCount.increment()
    }

    fun reportLockHoldDuration(lockTimeMs: Long) {
        soknadLockHoldTimer.record(lockTimeMs, TimeUnit.MILLISECONDS)
    }

    fun reportLockTimeout() = soknadLockTimeoutCount.increment()

    companion object {
        const val TAG_ETTERSENDELSE = "ettersendelse"
        const val TAG_MOTTAKER = "mottaker"
        const val TAG_STEG = "steg"
    }
}
