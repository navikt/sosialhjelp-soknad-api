package no.nav.sosialhjelp.soknad.app.soknadlock

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.IllegalStateException
import java.time.Clock
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

/**
 * Service som håndterer forsinkelse av forespørsler for å forhindre redigeringskonflikter.
 *
 * Denne tjenesten bruker en intern map (lockMap) for å holde styr på aktive låser basert på behandlingsId.
 * Den tilbyr funksjonalitet for å hente, frigjøre, og rense opp foreldede låser.
 *
 * PrometheusMetricsService brukes for å rapportere metrikkdata.
 *
 * @property lockMetrics Komponent for pushing av metrikker
 * @property clock Klokken som brukes for tidsstempel og tidshåndtering, kan overstyres for testing.
 */
@Component
class SoknadLockManager(
    private val lockMetrics: SoknadLockPushMetrics,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    // Om denne er false, vil ikke SoknadLockDelayInterceptor forsøke å hente låser.
    var enabled: Boolean = false

    // Lås per behandlingsId med timestamp for når låsen ble opprettet
    private val lockMap: ConcurrentHashMap<String, TimestampedLock> = ConcurrentHashMap()

    // Sist gang en prune ble kjørt
    private var lastPrune = ZonedDateTime.now(clock)
    private val lastPruneMutex = Mutex()

    companion object {
        /** Timeout for lastPruneMutex */
        const val PRUNE_LOCK_TIMEOUT_MS = 100L

        /** Timeout for låser, pt 1 sekund */
        const val LOCK_TIMEOUT_MS = 1000L

        /** Hvor lenge en behandlingsId skal vare */
        const val LOCK_EXPIRY_HOURS = 1L

        /** Hvor hyppig vi sjekker om det er på tide å kjøre pruneLocks */
        const val REQUEST_PRUNE_INTERVAL_MINUTES = 5L

        private val log = LoggerFactory.getLogger(SoknadLockManager::class.java)
    }

    /**
     * Prøver (i inntil LOCK_TIMEOUT_MS millisekunder) å få en lås for en gitt behandlingsId.
     *
     * Dersom det er mer enn REQUEST_PRUNE_INTERVAL_MINUTES siden sist gang denne er kalt vil den slette foreldede låser.
     *
     * @return TimestampedLock dersom låsen ble ervervet, ellers null.
     */
    fun getLock(behandlingsId: String): TimestampedLock? {
        val lock = lockMap.computeIfAbsent(behandlingsId) { TimestampedLock(clock) }

        val getLock = runCatching { runBlocking { withTimeout(LOCK_TIMEOUT_MS) { lock.lock() } } }.onFailure { lockMetrics.reportLockTimeout() }
            .onSuccess { lockMetrics.reportLockAcquireLatency(lock.nanosecondsSinceLockRequest) }

        if (isLockMapDueForPruning()) pruneLocks()

        return lock.takeIf { getLock.isSuccess }
    }

    /** Releaser en lås for en gitt behandlingsId og rapporterer metrikker. */
    fun releaseLock(lock: TimestampedLock) {
        lock.unlock()
        lockMetrics.reportLockHoldDuration(lock.nanosecondsSinceLockRequest)
    }

    /** Sletter foreldede låser fra lockMap. */
    fun pruneLocks() {
        val now = ZonedDateTime.now(clock)
        var numRemovedKeys = 0
        val expiredLocks = lockMap.filterValues { it.isExpiredBy(now) }
        log.info("Sletter ${expiredLocks.size} foreldede låser")

        expiredLocks.forEach { (behandlingsId, timestampedLock) ->
            // Dersom lockMap.remove kaster NPE, har den allerede blitt fjernet - det skal ikke kunne skje.
            // Det som derimot kan skje er at verdien endrer seg fordi låsen er blitt fornyet, da hopper vi bare videre.
            val removed = runCatching {
                lockMap.remove(behandlingsId, timestampedLock)
            }.onSuccess {
                if (!it) log.warn("lockMap endret under pruneLocks") // Denne bør fjernes når koden er kjørt inn.
            }.getOrElse {
                log.warn("i pruneLocks har $behandlingsId forsvunnet, skal ikke kunne skje", it)
                false
            }

            if (removed) {
                timestampedLock.let {
                    try {
                        it.unlock()
                        numRemovedKeys++
                    } catch (e: IllegalStateException) {
                        log.warn("kunne ikke låse opp lås før sletting, skal ikke skje", e)
                    }
                }
            }
        }

        log.debug("Slettet $numRemovedKeys foreldede låser")
    }

    /**
     * Teller antall låste entries i lockMap. Brukes kun til metrikker.
     */
    fun getNumLocks(): Int = lockMap.filterValues { it.isLocked }.size

    /**
     * Teller antall entries i lockMap. Brukes til metrikker og testing.
     */
    fun getLockMapSize(): Int = lockMap.size

    /**
     * Sjekker om en gitt behandlingsId eksisterer i lockMap. Brukes kun til testing.
     */
    fun hasLockMapEntry(behandlingsId: String): Boolean = lockMap[behandlingsId] != null

    /**
     * Sjekker om en gitt behandlingsId er låst. Brukes kun til testing.
     */
    fun isLocked(behandlingsId: String): Boolean = lockMap[behandlingsId]?.isLocked ?: false

    /**
     * Sjekker atomisk hvorvidt det er på tide å prune lockMap.
     *
     * Dersom ja, nullstill telleren og return true.
     *
     * @return true dersom det er på tide å kjøre pruneLocks, ellers false.
     */
    private fun isLockMapDueForPruning(): Boolean = runBlocking {
        try {
            withTimeout(PRUNE_LOCK_TIMEOUT_MS) {
                lastPruneMutex.withLock {
                    val now = ZonedDateTime.now(clock)
                    val isDueForPrune = now.isAfter(lastPrune.plusMinutes(REQUEST_PRUNE_INTERVAL_MINUTES))
                    if (isDueForPrune) lastPrune = now
                    isDueForPrune
                }
            }
        } catch (e: TimeoutCancellationException) {
            log.error("timeout for pruneMutex, skal ikke skje")
            false
        }
    }

    data class TimestampedLock(
        var lastLockAttempt: ZonedDateTime,
        val mutex: Mutex = Mutex(),
        val clock: Clock = Clock.systemDefaultZone()
    ) {
        constructor(clock: Clock) : this(ZonedDateTime.now(clock), Mutex(), clock)

        /**
         * Sets the last lock attempt timestamp and calls lock() on the underlying mutex
         * @see Mutex.lock
         */
        suspend fun lock() {
            this.lastLockAttempt = ZonedDateTime.now(clock)
            this.mutex.lock()
        }

        /**
         * Whether the underlying mutex is locked
         * @see Mutex.isLocked
         */
        val isLocked
            get(): Boolean = this.mutex.isLocked

        /**
         * Equivalent to calling `this.mutex.unlock()`
         * @see Mutex.unlock
         * */
        fun unlock() = this.mutex.unlock()

        /**
         * @return Nanoseconds since lock was last locked
         */
        val nanosecondsSinceLockRequest get(): Long = ChronoUnit.NANOS.between(ZonedDateTime.now(clock), lastLockAttempt)

        /**
         * @param expiry The timestamp to compare against
         * @return Whether the lock is expired by the given time
         */
        fun isExpiredBy(expiry: ZonedDateTime): Boolean = expiry.isAfter(lastLockAttempt.plusHours(LOCK_EXPIRY_HOURS))
    }
}
