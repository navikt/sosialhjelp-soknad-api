package no.nav.sosialhjelp.soknad.app.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.apache.commons.lang3.tuple.MutablePair
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Service
/**
 * Service som håndterer forsinkelse av forespørsler for å forhindre redigeringskonflikter.
 *
 * Denne tjenesten bruker en intern map (lockMap) for å holde styr på aktive låser basert på behandlingsId.
 * Den tilbyr funksjonalitet for å hente, frigjøre, og rense opp foreldede låser.
 *
 * PrometheusMetricsService brukes for å rapportere metrikkdata.
 *
 * @property meterRegistry Prometheus MeterRegistry for å rapportere metrikkdata.
 * @property clock Klokken som brukes for tidsstempel og tidshåndtering, kan overstyres for testing.
 */
class RequestDelayService(
    private val meterRegistry: MeterRegistry,
    private val clock: Clock = Clock.systemDefaultZone()
) {

    private val metricsGauges = mutableListOf<Gauge>()

    init {
        metricsGauges.add(Gauge.builder("soknad_lock_map_size") { lockMap.size.toDouble() }
            .description("Number of locks in the lock map")
            .register(meterRegistry))

        metricsGauges.add(Gauge.builder("soknad_lock_count") { lockMap.filterValues { (_, lock) -> lock.isLocked }.size.toDouble() }
            .description("Number of locks currently held")
            .register(meterRegistry))
    }

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


    // Timestamp for når denne tråden fikk låsen
    private val lockObtainedMs = ThreadLocal<Long>()

    // Lås per behandlingsId med timestamp for når låsen ble opprettet
    private val lockMap: ConcurrentHashMap<String, MutablePair<ZonedDateTime, Mutex>> = ConcurrentHashMap()

    // Sist gang en prune ble kjørt
    private var lastPrune = ZonedDateTime.now(clock)
    private val pruneTimeMutex = Mutex()

    companion object {
        const val LOCK_TIMEOUT_MS = 1000L

        // Hvor lenge en behandlingsId skal vare
        const val LOCK_EXPIRY_HOURS = 1L

        // Hvor hyppig vi sjekker om det er på tide å kjøre pruneLocks
        const val REQUEST_PRUNE_INTERVAL_MINUTES = 5L

        private val log = LoggerFactory.getLogger(RequestDelayService::class.java)
    }

    /**
     * Prøver (i opptil LOCK_TIMEOUT_MS millisekunder) å få en lås for en gitt behandlingsId.
     *
     * @return Mutex dersom låsen ble oppnådd, ellers null.
     */
    fun getLock(behandlingsId: String): Mutex? {
        // Construct if absent
        val lock = lockMap.computeIfAbsent(behandlingsId) { MutablePair(ZonedDateTime.now(clock), Mutex()) }

        // Prune locks if we're due for a run
        runBlocking { if (dueForPruning()) pruneLocks() }

        // Prometheus instrumentation timer
        lockObtainedMs.set(clock.instant().toEpochMilli())

        return runBlocking {
            try {
                withTimeout(LOCK_TIMEOUT_MS) { lock.right.lock() }
                reportLockAcquireLatency(System.currentTimeMillis() - lockObtainedMs.get())
                lock.right
            } catch (e: TimeoutCancellationException) {
                reportLockTimeout()
                null
            }
        }
    }

    private suspend fun dueForPruning(): Boolean = try {
        withTimeout(LOCK_TIMEOUT_MS) {
            pruneTimeMutex.withLock {
                val now = ZonedDateTime.now(clock)
                val due = now.isAfter(lastPrune.plusMinutes(REQUEST_PRUNE_INTERVAL_MINUTES))
                if (due) lastPrune = now
                due
            }
        }
    } catch (e: TimeoutCancellationException) {
        log.warn("timeout for pruneTimeMutex")
        false
    }

    /**
     *  Releaser lås for en gitt behandlingsId
     */
    fun releaseLock(lock: Mutex) {
        try {
            lock.unlock()
        } catch (e: Exception) {
            log.warn("Kunne ikke frigi lås", e)
        }
        reportLockHoldDuration(System.currentTimeMillis() - lockObtainedMs.get())
    }

    fun reportLockAcquireLatency(lockTimeMs: Long) = soknadLockLatencyTimer.record(lockTimeMs, TimeUnit.MILLISECONDS)

    fun reportLockHoldDuration(lockTimeMs: Long) = soknadLockHoldTimer.record(lockTimeMs, TimeUnit.MILLISECONDS)

    fun reportLockTimeout() = soknadLockTimeoutCount.increment()


    /**
     * Brukes kun av enhetstester
     */
    fun hasLock(behandlingsId: String): Boolean = lockMap[behandlingsId] != null

    fun pruneLocks() {
        val now = ZonedDateTime.now(clock)
        log.debug("Sjekker om foreldede låser skal slettes")
        val removedKeys = object : ThreadLocal<Int>() {
            override fun initialValue() = 0
        }

        val expiredLocks = lockMap.filterValues { (timestamp, _) -> now.isAfter(timestamp.plusHours(LOCK_EXPIRY_HOURS)) }

        expiredLocks.forEach { (key, value) ->
            if (!lockMap.remove(key, value)) {
                log.warn("Synkroniseringsproblem i pruneLocks")
                return@forEach
            }

            value.right.let {
                runCatching { it.unlock() }
                removedKeys.set(removedKeys.get() + 1)
            }
        }

        if (removedKeys.get() > 0) log.debug("Sletter ${removedKeys.get()} foreldede låser")
    }
}
