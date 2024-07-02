package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.nimbusds.jwt.SignedJWT
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadApiError
import no.nav.sosialhjelp.soknad.tilgangskontroll.XsrfGenerator
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("no-redis", "test", "test-container")
abstract class AbstractIntegrationTest {
    @Autowired
    protected lateinit var webTestClient: WebTestClient

    @Autowired
    protected lateinit var soknadRepository: SoknadRepository

    @Autowired
    protected lateinit var mockOAuth2Server: MockOAuth2Server

    protected lateinit var token: SignedJWT

    @BeforeEach
    fun before() {
        token = mockOAuth2Server.issueToken("selvbetjening", "54352345353", "someaudience", claims = mapOf("acr" to "idporten-loa-high"))
    }

    protected fun <T> doGet(
        uri: String,
        responseBodyClass: Class<T>,
    ): T {
        return webTestClient.get()
            .uri(uri)
            .header("Authorization", "Bearer ${token.serialize()}")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(responseBodyClass)
            .returnResult().responseBody!!
    }

    protected fun <T> doPut(
        uri: String,
        requestBody: Any,
        responseBodyClass: Class<T>,
        soknadId: UUID? = null,
    ): T {
        return webTestClient.put()
            .uri(uri)
            .header("Authorization", "Bearer ${token.serialize()}")
            .header("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken(soknadId?.toString(), id = token.jwtClaimsSet.subject))
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(requestBody))
            .exchange()
            .expectStatus().isOk
            .expectBody(responseBodyClass)
            .returnResult()
            .responseBody!!
    }

    protected fun doPutExpectError(
        uri: String,
        requestBody: Any,
        httpStatus: HttpStatus,
        soknadId: UUID? = null,
    ): SoknadApiError {
        return webTestClient.put()
            .uri(uri)
            .header("Authorization", "Bearer ${token.serialize()}")
            .header("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken(soknadId?.toString(), id = token.jwtClaimsSet.subject))
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(requestBody))
            .exchange()
            .expectStatus().isEqualTo(httpStatus)
            .expectBody(SoknadApiError::class.java)
            .returnResult()
            .responseBody!!
    }

    protected fun <T> doDelete(uri: String) {
        webTestClient
            .delete()
            .uri(uri)
            .header("Authorization", "Bearer ${token.serialize()}")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent
    }
}
