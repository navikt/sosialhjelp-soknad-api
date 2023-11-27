package no.nav.sosialhjelp.soknad.app.soknadlock

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class SoknadLockPushMetrics(
    meterRegistry: MeterRegistry
) {
    private val soknadLockLatencyTimer =
        Timer.builder("soknad_lock_acquire_latency")
            .description("Average latency for successful attempts to acquire lock")
            .register(meterRegistry)

    private val soknadLockHoldTimer = Timer.builder("soknad_lock_hold_duration")
        .description("Average time between a lock being held and released")
        .register(meterRegistry)

    private val soknadLockTimeoutCount = Counter.builder("soknad_lock_timeout_count")
        .description("Number of unsuccessful attempts to acquire a lock")
        .register(meterRegistry)

    fun reportLockAcquireLatency(lockTimeMs: Long) = soknadLockLatencyTimer.record(lockTimeMs, TimeUnit.NANOSECONDS)
    fun reportLockHoldDuration(lockTimeMs: Long) = soknadLockHoldTimer.record(lockTimeMs, TimeUnit.NANOSECONDS)
    fun reportLockTimeout() = soknadLockTimeoutCount.increment()
}
