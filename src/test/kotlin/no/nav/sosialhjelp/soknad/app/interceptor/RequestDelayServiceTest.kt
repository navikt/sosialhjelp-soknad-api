package no.nav.sosialhjelp.soknad.app.interceptor

import io.micrometer.core.instrument.MeterRegistry
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.app.service.RequestDelayService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

internal class RequestDelayServiceTest {
    private val mockMeterRegistry: MeterRegistry = mockk(relaxed = true)
    private lateinit var lockService: RequestDelayService
    private val mockClock: Clock = mockk()

    companion object {
        const val BEHANDLINGSID_A = "12345"
        const val BEHANDLINGSID_B = "56789"
    }

    @BeforeEach
    fun setup() {
        every { mockClock.instant() } returns Instant.now() // mock the current time
        every { mockClock.zone } returns ZoneId.systemDefault() // mock the zone

        lockService = RequestDelayService(mockMeterRegistry, mockClock)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test getLock acquires lock successfully`() {
        every { lockService.reportLockAcquireLatency(any()) } just runs
        lockService.getLock(BEHANDLINGSID_A) ?: fail("Lock should not be null.")
        verify { lockService.reportLockAcquireLatency(any()) }
    }

    @Test
    fun `test getLock fails to acquire lock after timeout`() = runBlocking {
        every { lockService.reportLockTimeout() } just runs

        // Acquire the lock on the main coroutine
        lockService.getLock(BEHANDLINGSID_A)

        // Launch a new coroutine to try and acquire the same lock
        val deferredLock = async(Dispatchers.Default) { lockService.getLock(BEHANDLINGSID_A) }

        val start = Instant.now()
        val lock = deferredLock.await()
        val duration = Duration.between(start, Instant.now())

        assertNull(lock)
        assertTrue(duration.toMillis() >= RequestDelayService.LOCK_TIMEOUT_MS)
        verify { lockService.reportLockTimeout() }
    }

    @Test
    fun `test releaseLock releases lock successfully`() {
        lockService.getLock(BEHANDLINGSID_A)?.let { lock ->
            lockService.releaseLock(lock)
            verify { lockService.reportLockHoldDuration(any()) }
        } ?: fail("Lock should not be null.")
    }

    @Test
    fun `test pruneLocks removes old locks`() {
        every { mockClock.instant() } returns Instant.now()

        lockService.getLock(BEHANDLINGSID_A)

        // Mock the time to be past the expiry
        every { mockClock.instant() } returns Instant.now().plus(Duration.ofHours(RequestDelayService.LOCK_EXPIRY_HOURS + 2))

        lockService.pruneLocks()

        assertFalse(lockService.hasLock(BEHANDLINGSID_A))
    }

    @Test
    fun `test old locks are pruned when getLock is called and prune is due`() = runBlocking {
        // Mock the clock to a past time
        val now = Instant.now()
        val later = now.plus(Duration.ofHours(RequestDelayService.LOCK_EXPIRY_HOURS + 2))
        every { mockClock.instant() } returns now

        // Acquire a lock
        assertNotNull(lockService.getLock(BEHANDLINGSID_A))

        // Ensure the lock is set
        assertTrue(lockService.hasLock(BEHANDLINGSID_A))

        // Move the clock forward beyond the lock expiry duration
        every { mockClock.instant() } returns later

        // Acquire a new lock, this should trigger pruning
        lockService.getLock(BEHANDLINGSID_B)

        // Verify that the old lock was pruned
        assertFalse(lockService.hasLock(BEHANDLINGSID_A))
    }

    @Test
    fun `test pruneLocks does not remove new locks`() {
        every { mockClock.instant() } returns Instant.now()

        lockService.getLock(BEHANDLINGSID_A)
        lockService.pruneLocks()

        assertTrue(lockService.hasLock(BEHANDLINGSID_A))
    }

    @Test
    fun `test hasLock returns false when lock is not present`() {
        assertFalse(lockService.hasLock(BEHANDLINGSID_A))
    }

    @Test
    fun `test hasLock returns true when lock is present`() {
        lockService.getLock(BEHANDLINGSID_A)
        assertTrue(lockService.hasLock(BEHANDLINGSID_A))
    }

    @Test
    fun `test two threads can acquire different locks`() = runBlocking {
        // Acquire a lock on the main coroutine
        lockService.getLock(BEHANDLINGSID_A)

        // Launch a new coroutine to acquire a different lock
        val deferredLock = async(Dispatchers.Default) {
            lockService.getLock(BEHANDLINGSID_B)
        }

        assertNotNull(deferredLock.await())
    }

    @Test
    fun `test two threads cannot acquire the same lock`() = runBlocking {
        // Acquire a lock on the main coroutine
        lockService.getLock(BEHANDLINGSID_A)

        // Launch a new coroutine to acquire the same lock
        val deferredLock = async(Dispatchers.Default) {
            lockService.getLock(BEHANDLINGSID_A)
        }

        assertNull(deferredLock.await())
    }
}
