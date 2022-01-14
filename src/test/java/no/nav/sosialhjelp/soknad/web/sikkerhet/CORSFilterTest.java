package no.nav.sosialhjelp.soknad.web.sikkerhet;

import no.nav.sosialhjelp.soknad.common.ServiceUtils;
import no.nav.sosialhjelp.soknad.web.rest.SoknadApplication;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.test.util.server.ContainerRequestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CORSFilterTest {

    @Mock
    private ServiceUtils serviceUtils;

    @InjectMocks
    private CORSFilter corsFilter;

    private final ContainerResponse response = mock(ContainerResponse.class);

    @BeforeEach
    public void setUp() throws Exception {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(response.getHeaders()).thenReturn(headers);
        when(serviceUtils.isNonProduction()).thenReturn(false);
    }

    @AfterEach
    public void tearDown() {
        System.clearProperty("environment.name");
    }

    @Test
    void setCorsHeaders_inProdWithUnknownOrigin_shouldNotSetCorsHeaders() {
        var unknownOrigin = "https://www.unknown.no";

        var request = ContainerRequestBuilder
                .from("requestUri", "GET", new SoknadApplication())
                .header("Origin", unknownOrigin)
                .build();

        corsFilter.filter(request, response);
        assertThat(response.getHeaders()).isEmpty();
    }

    @Test
    void setCorsHeaders_inProdWithTrustedOrigin_shouldSetCorsHeaders() {
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
    void setCorsHeaders_inTestWithUnknownOrigin_shouldSetCorsHeaders() {
        when(serviceUtils.isNonProduction()).thenReturn(true);

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