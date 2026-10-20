package no.nav.sosialhjelp.soknad.app.subjecthandler

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.sosialhjelp.soknad.app.Constants.SELVBETJENING
import no.nav.sosialhjelp.soknad.app.Constants.TOKENX
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class SubjectHandlerImplTest {
    private val holder = mockk<TokenValidationContextHolder>()
    private val handler = SubjectHandlerImpl(holder)

    @AfterEach
    fun clearRequest() = RequestContextHolder.resetRequestAttributes()

    @Test
    fun `uses coroutine token without a request`() {
        runBlocking(UserContextElement("background-token", "user")) {
            assertEquals("background-token", handler.getToken())
        }
        verify(exactly = 0) { holder.getTokenValidationContext() }
    }

    @Test
    fun `returns null when neither request nor coroutine has a token`() {
        assertNull(handler.getTokenOrNull())
        assertThrows(IllegalStateException::class.java) { handler.getToken() }
    }

    @Test
    fun `invalid request cannot use coroutine token`() {
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest()))
        every { holder.getTokenValidationContext() } returns
            mockk<TokenValidationContext> {
                every { hasValidToken() } returns false
            }

        runBlocking(UserContextElement("background-token", "user")) {
            val exception = assertThrows(RuntimeException::class.java) { handler.getTokenOrNull() }
            assertEquals("Could not find TokenValidationContext. Possibly no token in request.", exception.message)
        }
    }

    @Test
    fun `request with another issuer cannot use coroutine token`() {
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest()))
        every { holder.getTokenValidationContext() } returns
            mockk<TokenValidationContext> {
                every { hasValidToken() } returns true
                every { getJwtToken(TOKENX) } returns null
                every { getJwtToken(SELVBETJENING) } returns null
            }

        runBlocking(UserContextElement("background-token", "user")) {
            val exception = assertThrows(RuntimeException::class.java) { handler.getTokenOrNull() }
            assertEquals("Could not find TokenValidationContext. Possibly no token in request.", exception.message)
        }
    }

    @Test
    fun `validated request token wins over coroutine token`() {
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest()))
        every { holder.getTokenValidationContext() } returns
            mockk<TokenValidationContext> {
                every { hasValidToken() } returns true
                every { getJwtToken(TOKENX) } returns
                    mockk {
                        every { encodedToken } returns "request-token"
                    }
            }

        runBlocking(UserContextElement("background-token", "other-user")) {
            assertEquals("request-token", handler.getToken())
        }
    }
}
