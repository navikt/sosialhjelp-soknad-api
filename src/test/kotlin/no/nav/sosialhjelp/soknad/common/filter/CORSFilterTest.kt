package no.nav.sosialhjelp.soknad.common.filter

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.web.rest.SoknadApplication
import org.assertj.core.api.Assertions.assertThat
import org.glassfish.jersey.server.ContainerResponse
import org.glassfish.jersey.test.util.server.ContainerRequestBuilder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.MultivaluedMap

internal class CORSFilterTest {

    private val corsFilter = CORSFilter()
    private val response = mockk<ContainerResponse>()

    @BeforeEach
    fun setUp() {
        val headers: MultivaluedMap<String, Any> = MultivaluedHashMap()
        every {response.headers } returns headers
    }

    @AfterEach
    fun tearDown() {
        System.clearProperty("environment.name")
    }

    @Test
    fun setCorsHeaders_inProdWithUnknownOrigin_shouldNotSetCorsHeaders() {
        val unknownOrigin = "https://www.unknown.no"
        val request = ContainerRequestBuilder
            .from("requestUri", "GET", SoknadApplication())
            .header("Origin", unknownOrigin)
            .build()
        corsFilter.filter(request, response)
        assertThat(response.headers).isEmpty()
    }

    @Test
    fun setCorsHeaders_inProdWithTrustedOrigin_shouldSetCorsHeaders() {
        val trustedOrigin = "https://www.nav.no"
        val request = ContainerRequestBuilder
            .from("requestUri", "GET", SoknadApplication())
            .header("Origin", trustedOrigin)
            .build()
        corsFilter.filter(request, response)
        assertThat(response.headers).containsKey("Access-Control-Allow-Headers")
        assertThat(response.headers).containsKey("Access-Control-Allow-Methods")
        assertThat(response.headers).containsKey("Access-Control-Allow-Credentials")
        assertThat(response.headers["Access-Control-Allow-Origin"]!![0]).isEqualTo(trustedOrigin)
    }

    @Test
    fun setCorsHeaders_inTestWithUnknownOrigin_shouldSetCorsHeaders() {
        System.setProperty("environment.name", "q0")
        val unknownOrigin = "https://www.unknown.no"
        val request = ContainerRequestBuilder
            .from("requestUri", "GET", SoknadApplication())
            .header("Origin", unknownOrigin)
            .build()
        corsFilter.filter(request, response)
        assertThat(response.headers).containsKey("Access-Control-Allow-Headers")
        assertThat(response.headers).containsKey("Access-Control-Allow-Methods")
        assertThat(response.headers).containsKey("Access-Control-Allow-Credentials")
        assertThat(response.headers["Access-Control-Allow-Origin"]!![0]).isEqualTo(unknownOrigin)
    }
}
