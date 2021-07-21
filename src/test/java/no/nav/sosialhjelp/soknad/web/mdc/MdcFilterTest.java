package no.nav.sosialhjelp.soknad.web.mdc;

import no.nav.sosialhjelp.soknad.domain.model.oidc.OidcSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.rest.SoknadApplication;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.test.util.server.ContainerRequestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;

import static no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations.MDC_BEHANDLINGS_ID;
import static no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations.MDC_CALL_ID;
import static no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations.MDC_CONSUMER_ID;
import static no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations.getFromMDC;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CALL_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MdcFilterTest {
    private final static String MOCK_CALL_ID = "mock_call_id";
    private final static String MOCK_CONSUMER_ID = "mock_consumer_id";
    private final static String MOCK_BEHANDLINGS_ID = "mock_behandlings_id";

    @BeforeEach
    public void setUp() {
        System.setProperty("environment.name", "test");
        System.setProperty("systemuser.username", MOCK_CONSUMER_ID);
        SubjectHandler.setSubjectHandlerService(new OidcSubjectHandlerService());
    }

    @AfterEach
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");
        System.clearProperty("systemuser.username");
    }

    @Test
    void shouldAddCallIdFromRequest() {
        ContainerRequest request = ContainerRequestBuilder
                .from("requestUri", "GET", new SoknadApplication())
                .header(HEADER_CALL_ID, MOCK_CALL_ID)
                .build();

        MdcFilter filter = new MdcFilter();
        filter.filter(request);

        assertThat(getFromMDC(MDC_CALL_ID)).isEqualTo(MOCK_CALL_ID);
    }

    @Test
    void shouldGenerateCallIdIfNoneInRequest() {
        ContainerRequest request = ContainerRequestBuilder
                .from("requestUri", "GET", new SoknadApplication())
                .build();

        MdcFilter filter = new MdcFilter();
        filter.filter(request);

        assertThat(getFromMDC(MDC_CALL_ID)).contains("CallId_", "_");
    }

    @Test
    void shouldAddConsumerId() {
        ContainerRequest request = ContainerRequestBuilder
                .from("requestUri", "GET", new SoknadApplication())
                .build();

        MdcFilter filter = new MdcFilter();
        filter.filter(request);

        assertThat(getFromMDC(MDC_CONSUMER_ID)).isEqualTo(MOCK_CONSUMER_ID);
    }

    @Test
    void shouldAddBehandlingsId() {
        MultivaluedMap<String, String> pathParams = new MultivaluedHashMap<>();
        pathParams.put("behandlingsId", Collections.singletonList(MOCK_BEHANDLINGS_ID));

        ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
        when(uriInfo.getPathParameters()).thenReturn(pathParams);

        ContainerRequest request = mock(ContainerRequest.class);
        when(request.getHeaderString(HEADER_CALL_ID)).thenReturn(null);

        when(request.getUriInfo()).thenReturn(uriInfo);

        MdcFilter filter = new MdcFilter();
        filter.filter(request);

        assertThat(getFromMDC(MDC_BEHANDLINGS_ID)).isEqualTo(MOCK_BEHANDLINGS_ID);
    }
}
