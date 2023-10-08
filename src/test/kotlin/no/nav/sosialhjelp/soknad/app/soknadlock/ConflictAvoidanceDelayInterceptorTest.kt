package no.nav.sosialhjelp.soknad.app.soknadlock

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.sync.Mutex
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.servlet.HandlerMapping

@ExtendWith(SpringExtension::class)
internal class ConflictAvoidanceDelayInterceptorTest {

    private val soknadLockManager = mockk<SoknadLockManager>()

    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var handler: Any
    private lateinit var interceptor: ConflictAvoidanceDelayInterceptor

    companion object {
        const val BEHANDLINGSID_A = "123"
    }

    @BeforeEach
    fun setUp() {
        request = mockk(relaxed = true)
        response = mockk(relaxed = true)
        handler = mockk(relaxed = true)

        interceptor = ConflictAvoidanceDelayInterceptor(soknadLockManager)
    }

    private fun mockBehandlingsId(request: HttpServletRequest, behandlingsId: String?) {
        every {
            request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)
        } returns mapOf("behandlingsId" to behandlingsId)
    }

    @Test
    fun `should not attempt to lock for safe methods`() {
        listOf("GET", "HEAD", "OPTIONS").forEach { method ->
            every { request.method } returns method
            mockBehandlingsId(request, BEHANDLINGSID_A)

            interceptor.preHandle(request, response, handler)

            verify(exactly = 0) { soknadLockManager.getLock(any()) }
        }
    }

    @Test
    fun `should attempt to lock for unsafe methods`() {
        listOf("POST", "PUT", "DELETE", "PATCH").forEach { method ->
            every { request.method } returns method
            every { soknadLockManager.getLock(any()) } returns mockk()
            mockBehandlingsId(request, BEHANDLINGSID_A)

            interceptor.preHandle(request, response, handler)

            verify { soknadLockManager.getLock(BEHANDLINGSID_A) }
        }
    }

    @Test
    fun `should not attempt to lock if no behandlingsId in query`() {
        mockBehandlingsId(request, null)

        interceptor.preHandle(request, response, handler)

        verify(exactly = 0) { soknadLockManager.getLock(any()) }
    }

    @Test
    fun `should release lock after request completion`() {
        every { request.getAttribute(ConflictAvoidanceDelayInterceptor.LOCK_ATTRIBUTE_NAME) } returns Mutex()
        every { soknadLockManager.releaseLock(any()) } just runs

        interceptor.afterCompletion(request, response, handler, null)

        verify { soknadLockManager.releaseLock(any()) }
    }
}
