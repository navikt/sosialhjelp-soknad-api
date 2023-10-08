package no.nav.sosialhjelp.soknad.app.soknadlock

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.apache.commons.lang3.tuple.MutablePair
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.IllegalStateException
import java.time.Clock
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * Service som håndterer forsinkelse av forespørsler for å forhindre redigeringskonflikter.
 *
 * Denne tjenesten bruker en intern map (lockMap) for å holde styr på aktive låser basert på behandlingsId.
 * Den tilbyr funksjonalitet for å hente, frigjøre, og rense opp foreldede låser.
 *
 * PrometheusMetricsService brukes for å rapportere metrikkdata.
 *
 * @property lockMetrics Komponent for metrikker
 * @property clock Klokken som brukes for tidsstempel og tidshåndtering, kan overstyres for testing.
 */
@Component
class SoknadLockManager(
    private val lockMetrics: SoknadLockPushMetrics,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    // Timestamp for når denne tråden fikk låsen
    private val lockObtainedMs = ThreadLocal<Long>()

    // Lås per behandlingsId med timestamp for når låsen ble opprettet
    private val lockMap: ConcurrentHashMap<String, MutablePair<ZonedDateTime, Mutex>> = ConcurrentHashMap()

    // Sist gang en prune ble kjørt
    private var lastPrune = ZonedDateTime.now(clock)
    private val lastPruneMutex = Mutex()

    companion object {
        // Timeout for lastPruneMutex
        const val PRUNE_LOCK_TIMEOUT_MS = 100L

        // Timeout for låser
        const val LOCK_TIMEOUT_MS = 1000L

        // Hvor lenge en behandlingsId skal vare
        const val LOCK_EXPIRY_HOURS = 1L

        // Hvor hyppig vi sjekker om det er på tide å kjøre pruneLocks
        const val REQUEST_PRUNE_INTERVAL_MINUTES = 5L

        private val log = LoggerFactory.getLogger(SoknadLockManager::class.java)
    }

    /**
     * Prøver (i opptil LOCK_TIMEOUT_MS millisekunder) å få en lås for en gitt behandlingsId.
     *
     * Dersom det er mer enn REQUEST_PRUNE_INTERVAL_MINUTES siden sist gang denne er kalt,
     * vil den slette foreldede låser.
     *
     * @return Mutex dersom låsen ble oppnådd, ellers null.
     */
    fun getLock(behandlingsId: String): Mutex? {
        val mutex = makeOrTouchLock(behandlingsId)

        startStopwatch()

        val getLock = runCatching { runBlocking { withTimeout(LOCK_TIMEOUT_MS) { mutex.lock() } } }
            .onFailure { lockMetrics.reportLockTimeout() }
            .onSuccess { lockMetrics.reportLockAcquireLatency(getStopwatch()) }

        if (isLockMapDueForPruning()) pruneLocks()

        return mutex.takeIf { getLock.isSuccess }
    }

    /**
     *  Releaser en lås for en gitt behandlingsId og rapporterer metrikker.
     */
    fun releaseLock(lock: Mutex) {
        lock.unlock()
        lockMetrics.reportLockHoldDuration(getStopwatch())
    }

    /**
     * Sletter foreldede låser fra lockMap.
     */
    fun pruneLocks() {
        val now = ZonedDateTime.now(clock)
        var numRemovedKeys = 0
        val expiredLocks = lockMap.filterValues { (timestamp, _) -> now.isAfter(timestamp.plusHours(LOCK_EXPIRY_HOURS)) }
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
                val (_, lock) = timestampedLock

                lock.let {
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
    fun getNumLocks(): Int = lockMap.filterValues { (_, lock) -> lock.isLocked }.size

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
    fun isLocked(behandlingsId: String): Boolean = lockMap[behandlingsId]?.right?.isLocked ?: false

    /**
     * Sjekker atomisk hvorvidt det er på tide å prune lockMap.
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

    /**
     * Hent en Mutex for en gitt behandlingsId
     *
     * Oppretter atomisk et nytt element i lockMap dersom det ikke eksisterer,
     * oppdaterer timestamp dersom den allerede eksisterer
     */
    private fun makeOrTouchLock(behandlingsId: String): Mutex {
        val now = ZonedDateTime.now(clock)

        val entry = lockMap.computeIfAbsent(behandlingsId) { MutablePair(now, Mutex()) }
        entry.left = now

        return entry.right
    }

    /* Sets a thread-local timestamp */
    private fun startStopwatch() = lockObtainedMs.set(clock.instant().toEpochMilli())

    /* Gets milliseconds since stopwatch was started */
    private fun getStopwatch() = clock.instant().toEpochMilli() - lockObtainedMs.get()
}
