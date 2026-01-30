package no.nav.sosialhjelp.soknad.app.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_PATH
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_SOKNAD_ID
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE

// @Disabled
internal class MdcFilterTest {
    private val mdcFilter = MdcFilter()
    private val filterChain = MDCCapturingMockFilterChain()

    @BeforeEach
    fun setUp() {
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
    }

    @Test
    fun `should add path`() {
        val request = MockHttpServletRequest()
        request.requestURI = "requestUri"

        val response = MockHttpServletResponse()

        mdcFilter.doFilter(request, response, filterChain)

        assertThat(filterChain.capturedMDCValue(MDC_PATH)).isEqualTo("requestUri")
    }

    @Test
    fun `should add behandlingsId`() {
        val request = MockHttpServletRequest()
        request.setAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, mapOf("behandlingsId" to MOCK_BEHANDLINGS_ID))
        request.requestURI = "/sosialhjelp/soknad-api/soknader/$MOCK_BEHANDLINGS_ID/arbeid"

        val response = MockHttpServletResponse()

        mdcFilter.doFilter(request, response, filterChain)

        assertThat(filterChain.capturedMDCValue(MDC_SOKNAD_ID)).isEqualTo(MOCK_BEHANDLINGS_ID)
    }

    @Test
    fun `should not add behandlingsid for opprettSoknad`() {
        val request = MockHttpServletRequest()
        request.requestURI = "/sosialhjelp/soknad-api/soknader/opprettSoknad"

        val response = MockHttpServletResponse()

        mdcFilter.doFilter(request, response, filterChain)

        assertThatExceptionOfType(NoSuchElementException::class.java)
            .isThrownBy { filterChain.capturedMDCValue(MDC_SOKNAD_ID) }
    }

    companion object {
        private const val MOCK_BEHANDLINGS_ID = "mock_behandlings_id"
    }

    class MDCCapturingMockFilterChain : FilterChain {
        private var contextMap: MutableMap<String, String> = mutableMapOf()

        override fun doFilter(
            request: ServletRequest,
            response: ServletResponse,
        ) {
            contextMap = MDC.getCopyOfContextMap()
        }

        fun capturedMDCValue(key: String) = contextMap.getValue(key)
    }
}
