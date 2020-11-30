package no.nav.sbl.dialogarena.mdc;

import no.nav.sbl.dialogarena.config.TestSoknadApplication;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sts.StsSecurityConstants;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.test.util.server.ContainerRequestBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;

import static no.nav.sbl.dialogarena.mdc.MDCOperations.MDC_BEHANDLINGS_ID;
import static no.nav.sbl.dialogarena.mdc.MDCOperations.MDC_CALL_ID;
import static no.nav.sbl.dialogarena.mdc.MDCOperations.MDC_CONSUMER_ID;
import static no.nav.sbl.dialogarena.mdc.MDCOperations.getFromMDC;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_CALL_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.StringContainsInOrder.stringContainsInOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MdcFilterTest {
    private final static String MOCK_CALL_ID = "mock_call_id";
    private final static String MOCK_CONSUMER_ID = "mock_consumer_id";
    private final static String MOCK_BEHANDLINGS_ID = "mock_behandlings_id";

    @BeforeClass
    public static void setUp() {
        System.setProperty(StsSecurityConstants.SYSTEMUSER_USERNAME, MOCK_CONSUMER_ID);
        SubjectHandler.setSubjectHandlerService(new OidcSubjectHandlerService());
    }

    @AfterClass
    public static void cleanUp() {
        System.clearProperty(StsSecurityConstants.SYSTEMUSER_USERNAME);
        SubjectHandler.resetOidcSubjectHandlerService();
    }

    @Test
    public void shouldAddCallIdFromRequest() {
        ContainerRequest request = ContainerRequestBuilder
                .from("requestUri", "GET", new TestSoknadApplication())
                .header(HEADER_CALL_ID, MOCK_CALL_ID)
                .build();

        MdcFilter filter = new MdcFilter();
        filter.filter(request);

        assertThat(getFromMDC(MDC_CALL_ID), is(MOCK_CALL_ID));
    }

    @Test
    public void shouldGenerateCallIdIfNoneInRequest() {
        ContainerRequest request = ContainerRequestBuilder
                .from("requestUri", "GET", new TestSoknadApplication())
                .build();

        MdcFilter filter = new MdcFilter();
        filter.filter(request);

        assertThat(getFromMDC(MDC_CALL_ID), stringContainsInOrder("CallId_", "_"));
    }

    @Test
    public void shouldAddConsumerId() {
        ContainerRequest request = ContainerRequestBuilder
                .from("requestUri", "GET", new TestSoknadApplication())
                .build();

        MdcFilter filter = new MdcFilter();
        filter.filter(request);

        assertThat(getFromMDC(MDC_CONSUMER_ID), is(MOCK_CONSUMER_ID));
    }

    @Test
    public void shouldAddBehandlingsId() {
        MultivaluedMap<String, String> pathParams = new MultivaluedHashMap();
        pathParams.put("behandlingsId", Collections.singletonList(MOCK_BEHANDLINGS_ID));

        ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
        when(uriInfo.getPathParameters()).thenReturn(pathParams);

        ContainerRequest request = mock(ContainerRequest.class);
        when(request.getHeaderString(HEADER_CALL_ID)).thenReturn(null);

        when(request.getUriInfo()).thenReturn(uriInfo);

        MdcFilter filter = new MdcFilter();
        filter.filter(request);

        assertThat(getFromMDC(MDC_BEHANDLINGS_ID), is(MOCK_BEHANDLINGS_ID));
    }
}
