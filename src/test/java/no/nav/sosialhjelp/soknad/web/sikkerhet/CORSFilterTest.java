package no.nav.sosialhjelp.soknad.web.sikkerhet;

import no.nav.sosialhjelp.soknad.web.rest.SoknadApplication;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.test.util.server.ContainerRequestBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CORSFilterTest {

    private final CORSFilter corsFilter = new CORSFilter();

    private final ContainerResponse response = mock(ContainerResponse.class);

    @Before
    public void setUp() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(response.getHeaders()).thenReturn(headers);
    }

    @After
    public void tearDown() {
        System.clearProperty("environment.name");
    }

    @Test
    public void setCorsHeaders_inProdWithUnknownOrigin_shouldNotSetCorsHeaders() {
        var unknownOrigin = "https://www.unknown.no";

        var request = ContainerRequestBuilder
                .from("requestUri", "GET", new SoknadApplication())
                .header("Origin", unknownOrigin)
                .build();

        corsFilter.filter(request, response);
        assertThat(response.getHeaders()).isEmpty();
    }

    @Test
    public void setCorsHeaders_inProdWithTrustedOrigin_shouldSetCorsHeaders() {
        var trustedOrigin = "https://www.nav.no";

        var request = ContainerRequestBuilder
                .from("requestUri", "GET", new SoknadApplication())
                .header("Origin", trustedOrigin)
                .build();

        corsFilter.filter(request, response);

        assertThat(response.getHeaders()).containsKey("Access-Control-Allow-Headers");
        assertThat(response.getHeaders()).containsKey("Access-Control-Allow-Methods");
        assertThat(response.getHeaders()).containsKey("Access-Control-Allow-Credentials");
        assertThat(response.getHeaders().get("Access-Control-Allow-Origin").get(0)).isEqualTo(trustedOrigin);
    }

    @Test
    public void setCorsHeaders_inTestWithUnknownOrigin_shouldSetCorsHeaders() {
        System.setProperty("environment.name", "q0");

        var unknownOrigin = "https://www.unknown.no";

        var request = ContainerRequestBuilder
                .from("requestUri", "GET", new SoknadApplication())
                .header("Origin", unknownOrigin)
                .build();

        corsFilter.filter(request, response);

        assertThat(response.getHeaders()).containsKey("Access-Control-Allow-Headers");
        assertThat(response.getHeaders()).containsKey("Access-Control-Allow-Methods");
        assertThat(response.getHeaders()).containsKey("Access-Control-Allow-Credentials");
        assertThat(response.getHeaders().get("Access-Control-Allow-Origin").get(0)).isEqualTo(unknownOrigin);
    }
}