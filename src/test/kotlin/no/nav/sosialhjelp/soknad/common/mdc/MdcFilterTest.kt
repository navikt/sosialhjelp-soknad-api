package no.nav.sosialhjelp.soknad.common.mdc

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.common.subjecthandler.AzureAdSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations.MDC_BEHANDLINGS_ID
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations.getFromMDC
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.web.rest.SoknadApplication
import org.assertj.core.api.Assertions.assertThat
import org.glassfish.jersey.server.ContainerRequest
import org.glassfish.jersey.server.ExtendedUriInfo
import org.glassfish.jersey.test.util.server.ContainerRequestBuilder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap

internal class MdcFilterTest {

    @BeforeEach
    fun setUp() {
        System.setProperty("environment.name", "test")
        System.setProperty("systemuser.username", MOCK_CONSUMER_ID)
        SubjectHandlerUtils.setNewSubjectHandlerImpl(AzureAdSubjectHandlerImpl())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        System.clearProperty("environment.name")
        System.clearProperty("systemuser.username")
    }

    @Test
    fun shouldAddCallIdFromRequest() {
        val request = ContainerRequestBuilder
            .from("requestUri", "GET", SoknadApplication())
            .header(HEADER_CALL_ID, MOCK_CALL_ID)
            .build()

        val filter = MdcFilter()
        filter.filter(request)

        assertThat(getFromMDC(MDC_CALL_ID)).isEqualTo(MOCK_CALL_ID)
    }

    @Test
    fun shouldGenerateCallIdIfNoneInRequest() {
        val request = ContainerRequestBuilder
            .from("requestUri", "GET", SoknadApplication())
            .build()

        val filter = MdcFilter()
        filter.filter(request)

        assertThat(getFromMDC(MDC_CALL_ID)).contains("CallId_", "_")
    }

    @Test
    fun shouldAddConsumerId() {
        val request = ContainerRequestBuilder
            .from("requestUri", "GET", SoknadApplication())
            .build()

        val filter = MdcFilter()
        filter.filter(request)

        assertThat(getFromMDC(MDCOperations.MDC_CONSUMER_ID)).isEqualTo(MOCK_CONSUMER_ID)
    }

    @Test
    fun shouldAddBehandlingsId() {
        val pathParams: MultivaluedMap<String, String> = MultivaluedHashMap()
        pathParams["behandlingsId"] = listOf(MOCK_BEHANDLINGS_ID)
        val uriInfo: ExtendedUriInfo = mockk()
        every { uriInfo.pathParameters } returns pathParams
        val request: ContainerRequest = mockk()
        every { request.getHeaderString(HEADER_CALL_ID) } returns null
        every { request.uriInfo } returns uriInfo

        val filter = MdcFilter()
        filter.filter(request)

        assertThat(getFromMDC(MDC_BEHANDLINGS_ID)).isEqualTo(MOCK_BEHANDLINGS_ID)
    }

    companion object {
        private const val MOCK_CALL_ID = "mock_call_id"
        private const val MOCK_CONSUMER_ID = "mock_consumer_id"
        private const val MOCK_BEHANDLINGS_ID = "mock_behandlings_id"
    }
}
