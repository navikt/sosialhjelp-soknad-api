package no.nav.sosialhjelp.soknad.app.mdc

import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.app.filter.MdcFilter
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_BEHANDLINGS_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_CONSUMER_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.getFromMDC
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

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
    fun `should add CallId from request`() {
        val request = MockHttpServletRequest()
        request.requestURI = "requestUri"
        request.addHeader(HEADER_CALL_ID, MOCK_CALL_ID)

        val response = MockHttpServletResponse()

        mdcFilter.doFilter(request, response, filterChain)

        assertThat(filterChain.capturedMDCValue(MDC_CALL_ID)).isEqualTo(MOCK_CALL_ID)
    }

    @Test
    fun `should generate CallId if none in request`() {
        val request = MockHttpServletRequest()
        request.requestURI = "requestUri"

        val response = MockHttpServletResponse()

        mdcFilter.doFilter(request, response, filterChain)

        assertThat(filterChain.capturedMDCValue(MDC_CALL_ID)).contains("CallId_", "_")
    }

    @Test
    fun `should add consumerId`() {
        val request = MockHttpServletRequest()
        request.requestURI = "requestUri"

        val response = MockHttpServletResponse()

        mdcFilter.doFilter(request, response, filterChain)

        assertThat(filterChain.capturedMDCValue(MDC_CONSUMER_ID)).isEqualTo("StaticConsumerId")
    }

    @Test
    fun `should add behandlingsId`() {
        val request = MockHttpServletRequest()
        request.requestURI = "/sosialhjelp/soknad-api/soknader/$MOCK_BEHANDLINGS_ID/arbeid"

        val response = MockHttpServletResponse()

        mdcFilter.doFilter(request, response, filterChain)

        assertThat(filterChain.capturedMDCValue(MDC_BEHANDLINGS_ID)).isEqualTo(MOCK_BEHANDLINGS_ID)
    }

    @Test
    fun `should clear mdc context afterwards`() {
        val request = MockHttpServletRequest()
        request.requestURI = "requestUri"
        request.addHeader(HEADER_CALL_ID, MOCK_CALL_ID)

        val response = MockHttpServletResponse()

        mdcFilter.doFilter(request, response, MockFilterChain())

        assertThat(getFromMDC(MDC_CALL_ID)).isNull()
    }

    companion object {
        private const val MOCK_CALL_ID = "mock_call_id"
        private const val MOCK_BEHANDLINGS_ID = "mock_behandlings_id"
    }

    class MDCCapturingMockFilterChain : FilterChain {
        private var contextMap: MutableMap<String, String> = mutableMapOf()

        override fun doFilter(request: ServletRequest, response: ServletResponse) {
            contextMap = MDC.getCopyOfContextMap()
        }

        fun capturedMDCValue(key: String) = contextMap.getValue(key)
    }
}
