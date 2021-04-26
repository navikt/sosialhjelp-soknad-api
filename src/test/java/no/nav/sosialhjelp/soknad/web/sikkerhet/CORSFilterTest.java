package no.nav.sosialhjelp.soknad.web.sikkerhet;

import org.junit.After;
import org.junit.Test;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CORSFilterTest {

    private final CORSFilter corsFilter = new CORSFilter();
    private final String unknownOrigin = "https://www.unknown.no";

    private ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
    private ContainerResponseContext responseContext = mock(ContainerResponseContext.class);

    @After
    public void tearDown() {
        System.clearProperty("environment.name");
    }

    @Test
    public void setCorsHeaders_inProdWithUnknownOrigin_shouldNotSetCorsHeaders() {
        when(requestContext.getHeaderString("Origin")).thenReturn(unknownOrigin);
        when(responseContext.getHeaders()).thenReturn(new MultivaluedHashMap<>());
        corsFilter.filter(requestContext, responseContext);

        assertThat(responseContext.getHeaders()).isEmpty();
    }

    @Test
    public void setCorsHeaders_inProdWithTrustedOrigin_shouldSetCorsHeaders() {
        String trustedOrigin = "https://www.nav.no";

        when(requestContext.getHeaderString("Origin")).thenReturn(trustedOrigin);
        when(responseContext.getHeaders()).thenReturn(new MultivaluedHashMap<>());
        corsFilter.filter(requestContext, responseContext);

        var headers = responseContext.getHeaders();
        assertThat(headers).hasSize(4);
        assertThat(headers.getFirst("Access-Control-Allow-Origin")).isEqualTo(trustedOrigin);
        assertThat(headers.containsKey("Access-Control-Allow-Headers")).isTrue();
        assertThat(headers.containsKey("Access-Control-Allow-Methods")).isTrue();
        assertThat(headers.containsKey("Access-Control-Allow-Credentials")).isTrue();
    }

    @Test
    public void setCorsHeaders_inTestWithUnknownOrigin_shouldSetCorsHeaders() {
        System.setProperty("environment.name", "q0");

        when(requestContext.getHeaderString("Origin")).thenReturn(unknownOrigin);
        when(responseContext.getHeaders()).thenReturn(new MultivaluedHashMap<>());
        corsFilter.filter(requestContext, responseContext);

        var headers = responseContext.getHeaders();
        assertThat(headers).hasSize(4);
        assertThat(headers.getFirst("Access-Control-Allow-Origin")).isEqualTo(unknownOrigin);
        assertThat(headers.containsKey("Access-Control-Allow-Headers")).isTrue();
        assertThat(headers.containsKey("Access-Control-Allow-Methods")).isTrue();
        assertThat(headers.containsKey("Access-Control-Allow-Credentials")).isTrue();
    }
}