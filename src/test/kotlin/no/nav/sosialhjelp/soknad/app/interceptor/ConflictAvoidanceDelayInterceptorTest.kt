package no.nav.sosialhjelp.soknad.app.interceptor

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.sync.Mutex
import no.nav.sosialhjelp.soknad.app.service.RequestDelayService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.servlet.HandlerMapping

@ExtendWith(SpringExtension::class)
internal class ConflictAvoidanceDelayInterceptorTest {

    private val requestDelayService = mockk<RequestDelayService>()

    lateinit var request: HttpServletRequest
    lateinit var response: HttpServletResponse
    lateinit var handler: Any

    lateinit var interceptor: ConflictAvoidanceDelayInterceptor

    companion object {
        const val BEHANDLINGS_ID_A = "123"
    }

    @BeforeEach
    fun setUp() {
        request = mockk(relaxed = true)
        response = mockk(relaxed = true)
        handler = mockk(relaxed = true)

        interceptor = ConflictAvoidanceDelayInterceptor(requestDelayService)
    }

    @Test
    fun `should not attempt to lock for safe methods`() {
        listOf("GET", "HEAD", "OPTIONS").forEach { method ->
            every { request.method } returns method
            every { request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) } returns mapOf("behandlingsId" to BEHANDLINGS_ID_A)

            interceptor.preHandle(request, response, handler)

            verify(exactly = 0) { requestDelayService.getLock(any()) }
        }
    }

    @Test
    fun `should attempt to lock for unsafe methods`() {
        listOf("POST", "PUT", "DELETE", "PATCH").forEach { method ->
            every { request.method } returns method
            every { requestDelayService.getLock(any()) } returns mockk()
            every { request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) } returns mapOf("behandlingsId" to BEHANDLINGS_ID_A)

            interceptor.preHandle(request, response, handler)

            verify { requestDelayService.getLock(BEHANDLINGS_ID_A) }
        }
    }

    @Test
    fun `should not attempt to lock without acquiredLock`() {
        every { request.getAttribute("acquiredLock") } returns null

        interceptor.preHandle(request, response, handler)

        verify(exactly = 0) { requestDelayService.getLock(any()) }
    }

    @Test
    fun `should release lock after request completion`() {
        every { request.getAttribute("acquiredLock") } returns Mutex()
        every { requestDelayService.releaseLock(any()) } just runs

        interceptor.afterCompletion(request, response, handler, null)

        verify { requestDelayService.releaseLock(any()) }
    }
}
