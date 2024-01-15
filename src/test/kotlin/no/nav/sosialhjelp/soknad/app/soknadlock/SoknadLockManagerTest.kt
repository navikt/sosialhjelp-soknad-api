package no.nav.sosialhjelp.soknad.app.soknadlock

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.CountDownLatch

internal class SoknadLockManagerTest {
    private val mockLockMetrics: SoknadLockPushMetrics = mockk(relaxed = true)
    private lateinit var lockManager: SoknadLockManager
    private val mockClock: Clock = mockk()

    companion object {
        const val BEHANDLINGSID_A = "12345"
        const val BEHANDLINGSID_B = "56789"
        const val SLINGRINGSMONN_MS = 100L
    }

    @BeforeEach
    fun setup() {
        every { mockClock.instant() } returns Instant.now() // mock the current time
        every { mockClock.zone } returns ZoneId.systemDefault() // mock the zone
        every { mockLockMetrics.reportLockAcquireLatency(any()) } just runs
        every { mockLockMetrics.reportLockTimeout() } just runs
        every { mockLockMetrics.reportLockHoldDuration(any()) } just runs
        lockManager = SoknadLockManager(mockLockMetrics, mockClock)
        assertLockMapState(mapOf())
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test getLock acquires lock successfully`() {
        lockManager.getLock(BEHANDLINGSID_A) ?: fail("Lock should not be null.")
        assertLockMapState(mapOf(BEHANDLINGSID_A to LockState.LOCKED))
    }

    @Test
    fun `test getLock fails to acquire lock twice on different threads`() = runBlocking {
        val (duration, locks) = measureTimeAndResult {
            runSimultaneously({
                lockManager.getLock(BEHANDLINGSID_A)
            }, {
                lockManager.getLock(BEHANDLINGSID_A)
            })
        }

        assertLockMapState(mapOf(BEHANDLINGSID_A to LockState.LOCKED))

        verify { mockLockMetrics.reportLockTimeout() }
        verify { mockLockMetrics.reportLockAcquireLatency(any()) }
        verify(exactly = 0) { mockLockMetrics.reportLockHoldDuration(any()) }

        assertEquals(1, locks.filterNotNull().size, "expected only one lock to be acquired")
        assertEquals(1, locks.filter { it == null }.size, "expected one lock to fail to acquire")
        assertLockMapState(mapOf(BEHANDLINGSID_A to LockState.LOCKED), "after timeout getting second lock on BEHANDLINGSID_A")
        assertTrue(
            (duration.toMillis() + SLINGRINGSMONN_MS) >= SoknadLockManager.LOCK_TIMEOUT_MS,
            "expected to wait at least ${SoknadLockManager.LOCK_TIMEOUT_MS}ms",
        )
    }

    @Test
    fun `test getLock fails to acquire lock twice on same thread`() = runBlocking {
        lockManager.getLock(BEHANDLINGSID_A) ?: fail("expected to acquire lock on BEHANDLINGSID_A")

        assertLockMapState(mapOf(BEHANDLINGSID_A to LockState.LOCKED))

        // Try to acquire the same lock again
        val (duration, lock) = measureTimeAndResult { lockManager.getLock(BEHANDLINGSID_A) }

        assertLockMapState(mapOf(BEHANDLINGSID_A to LockState.LOCKED))
        assertTrue((duration.toMillis() + SLINGRINGSMONN_MS) >= SoknadLockManager.LOCK_TIMEOUT_MS)
        assertNull(lock)
    }

    @Test
    fun `test releaseLock releases lock successfully`() {
        val lock = lockManager.getLock(BEHANDLINGSID_A) ?: fail("Lock should not be null.")
        assertLockMapState(mapOf(BEHANDLINGSID_A to LockState.LOCKED))

        lockManager.releaseLock(lock)
        assertLockMapState(mapOf(BEHANDLINGSID_A to LockState.UNLOCKED))
    }

    @Test
    fun `test two threads can acquire different locks`() = runBlocking {
        runSimultaneously({
            lockManager.getLock(BEHANDLINGSID_A) ?: fail("expected to acquire lock on BEHANDLINGSID_A")
        }, {
            lockManager.getLock(BEHANDLINGSID_B) ?: fail("expected to acquire lock on BEHANDLINGSID_B")
        })

        assertLockMapState(
            mapOf(BEHANDLINGSID_A to LockState.LOCKED, BEHANDLINGSID_B to LockState.LOCKED),
            "after acquiring locks",
        )
    }

    @Test
    fun `test pruneLocks removes old locks`() {
        lockManager.getLock(BEHANDLINGSID_A)
        assertLockMapState(mapOf(BEHANDLINGSID_A to LockState.LOCKED))

        // Mock the time to be past the expiry
        every { mockClock.instant() } returns Instant.now().plus(Duration.ofHours(SoknadLockManager.LOCK_EXPIRY_HOURS + 2))

        lockManager.pruneLocks()
        assertLockMapState(mapOf(BEHANDLINGSID_A to LockState.ABSENT))
    }

    @Test
    fun `test old locks are pruned when getLock is called and prune is due`() = runBlocking {
        lockManager.getLock(BEHANDLINGSID_A) ?: fail("expected to acquire lock on BEHANDLINGSID_A")
        assertLockMapState(mapOf(BEHANDLINGSID_A to LockState.LOCKED))

        // Move the clock forward beyond the lock expiry duration
        every { mockClock.instant() } returns afterLockExpires()

        // Acquire a new lock, this should trigger pruning
        lockManager.getLock(BEHANDLINGSID_B) ?: fail("expected to acquire lock on BEHANDLINGSID_B")

        assertLockMapState(mapOf(BEHANDLINGSID_A to LockState.ABSENT, BEHANDLINGSID_B to LockState.LOCKED))
    }

    @Test
    fun `test pruneLocks does not remove new locks`() {
        lockManager.getLock(BEHANDLINGSID_A) ?: fail("expected to acquire lock on BEHANDLINGSID_A")
        lockManager.pruneLocks()
        assertLockMapState(mapOf(BEHANDLINGSID_A to LockState.LOCKED))
    }

    /**
     * Metrics tests
     */

    @Test
    fun `test failed getLock reports metrics`() = runBlocking {
        // Acquire the lock on the main coroutine
        lockManager.getLock(BEHANDLINGSID_A) ?: fail("expected to acquire lock on BEHANDLINGSID_A")
        assertLockMapState(mapOf(BEHANDLINGSID_A to LockState.LOCKED), "after acquiring lock on BEHANDLINGSID_A")

        // Launch a new coroutine to try and acquire the same lock
        val secondLock = async(Dispatchers.Default) { lockManager.getLock(BEHANDLINGSID_A) }.await()

        assertNull(secondLock, "expected to fail to acquire second lock on BEHANDLINGSID_A")
        assertLockMapState(mapOf(BEHANDLINGSID_A to LockState.LOCKED), "after timeout getting lock on BEHANDLINGSID_A")
        verify { mockLockMetrics.reportLockTimeout() }
    }

    @Test
    fun `test unlock reports metrics`() {
        lockManager.releaseLock(lockManager.getLock(BEHANDLINGSID_A) ?: fail("Lock should not be null."))
        assertLockMapState(mapOf(BEHANDLINGSID_A to LockState.UNLOCKED))
        verify { mockLockMetrics.reportLockAcquireLatency(any()) }
        verify { mockLockMetrics.reportLockHoldDuration(any()) }
    }

    /**
     * Testing helper tests
     */

    @Test
    fun `test hasLockMapEntry returns false when lock is not present`() {
        assertFalse(lockManager.hasLockMapEntry(BEHANDLINGSID_A), "expected BEHANDLINGSID_A to be absent")
    }

    @Test
    fun `test hasLockMapEntry returns true when lock is present`() {
        lockManager.getLock(BEHANDLINGSID_A)
        assertTrue(lockManager.hasLockMapEntry(BEHANDLINGSID_A), "expected BEHANDLINGSID_A to be present")
    }

    @Test
    fun `test isLocked returns false when lock is absent`() {
        assertFalse(lockManager.isLocked(BEHANDLINGSID_A), "expected BEHANDLINGSID_A to be absent")
    }

    @Test
    fun `test isLocked returns false when lock is present, but unlocked`() {
        lockManager.releaseLock(lockManager.getLock(BEHANDLINGSID_A) ?: fail("Lock should not be null."))
        assertFalse(lockManager.isLocked(BEHANDLINGSID_A), "expected BEHANDLINGSID_A to be unlocked")
    }

    @Test
    fun `test isLocked returns true when lock is present and locked`() {
        lockManager.getLock(BEHANDLINGSID_A) ?: fail("expected to acquire lock on BEHANDLINGSID_A")
        assertTrue(lockManager.isLocked(BEHANDLINGSID_A), "expected BEHANDLINGSID_A to be locked")
    }

    private fun assertLockMapState(stateMap: Map<String, LockState>, message: String? = null) {
        val postfix = when (message != null) {
            true -> " $message"
            false -> ""
        }

        val expectedLockMapSize = stateMap.filterValues { it != LockState.ABSENT }.size
        assertEquals(expectedLockMapSize, lockManager.getLockMapSize(), "expected $expectedLockMapSize locks in map" + postfix)
        stateMap.forEach {
            when (it.value) {
                LockState.LOCKED -> {
                    assertTrue(lockManager.hasLockMapEntry(it.key), "expected ${it.key} to be present" + postfix)
                    assertTrue(lockManager.isLocked(it.key), "expected ${it.key} to be locked" + postfix)
                }

                LockState.UNLOCKED -> {
                    assertTrue(lockManager.hasLockMapEntry(it.key), "expected ${it.key} to be present" + postfix)
                    assertFalse(lockManager.isLocked(it.key), "expected ${it.key} to be unlocked" + postfix)
                }

                LockState.ABSENT -> {
                    assertFalse(lockManager.hasLockMapEntry(it.key), "expected ${it.key} to be absent" + postfix)
                    assertFalse(lockManager.isLocked(it.key), "expected ${it.key} to be unlocked" + postfix)
                }
            }
        }
    }

    private inline fun <T> measureTimeAndResult(block: () -> T): Pair<Duration, T> {
        val start = Instant.now()
        val result = block()
        val end = Instant.now()
        return Duration.between(start, end) to result
    }

    private fun <T> runSimultaneously(vararg blocks: suspend () -> T): List<T> = runBlocking {
        val latch = CountDownLatch(blocks.size)

        // Create and start the coroutines
        val deferredList = blocks.map { block ->
            async(Dispatchers.Default) {
                withContext(Dispatchers.IO) {
                    latch.countDown()
                    latch.await()
                    block()
                }
            }
        }

        // Wait for all coroutines to complete
        awaitAll(*deferredList.toTypedArray())
    }

    enum class LockState {
        LOCKED, UNLOCKED, ABSENT
    }

    private fun afterLockExpires(): Instant = Instant.now().plus(Duration.ofHours(SoknadLockManager.LOCK_EXPIRY_HOURS + 2))
}
