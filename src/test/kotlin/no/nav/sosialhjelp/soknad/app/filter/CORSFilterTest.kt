package no.nav.sosialhjelp.soknad.app.filter

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

internal class CORSFilterTest {

    private val corsFilter = CORSFilter()

    private val filterChain = MockFilterChain()

    @BeforeEach
    fun setUp() {
        mockkObject(MiljoUtils)
    }

    @AfterEach
    internal fun tearDown() {
        unmockkObject(MiljoUtils)
    }

    @Test
    fun setCorsHeaders_inProdWithUnknownOrigin_shouldNotSetCorsHeaders() {
        every { MiljoUtils.isNonProduction() } returns false

        val unknownOrigin = "https://www.unknown.no"

        val request = MockHttpServletRequest()
        request.requestURI = "requestUri"
        request.addHeader("Origin", unknownOrigin)

        val response = MockHttpServletResponse()

        corsFilter.doFilter(request, response, filterChain)

        assertThat(response.headerNames).isEmpty()
    }

    @Test
    fun setCorsHeaders_inProdWithTrustedOrigin_shouldSetCorsHeaders() {
        every { MiljoUtils.isNonProduction() } returns true
        val trustedOrigin = "https://www.nav.no"

        val request = MockHttpServletRequest()
        request.requestURI = "requestUri"
        request.addHeader("Origin", trustedOrigin)

        val response = MockHttpServletResponse()

        corsFilter.doFilter(request, response, filterChain)
        assertThat(response.headerNames).contains("Access-Control-Allow-Headers")
        assertThat(response.headerNames).contains("Access-Control-Allow-Methods")
        assertThat(response.headerNames).contains("Access-Control-Allow-Credentials")
        assertThat(response.getHeader("Access-Control-Allow-Origin")).isEqualTo(trustedOrigin)
    }

    @Test
    fun setCorsHeaders_inTestWithUnknownOrigin_shouldSetCorsHeaders() {
        every { MiljoUtils.isNonProduction() } returns true
        val unknownOrigin = "https://www.unknown.no"

        val request = MockHttpServletRequest()
        request.requestURI = "requestUri"
        request.addHeader("Origin", unknownOrigin)

        val response = MockHttpServletResponse()

        corsFilter.doFilter(request, response, filterChain)

        assertThat(response.headerNames).contains("Access-Control-Allow-Headers")
        assertThat(response.headerNames).contains("Access-Control-Allow-Methods")
        assertThat(response.headerNames).contains("Access-Control-Allow-Credentials")
        assertThat(response.getHeader("Access-Control-Allow-Origin")).isEqualTo(unknownOrigin)
    }
}
