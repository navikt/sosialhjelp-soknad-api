package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.nimbusds.jwt.SignedJWT
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.sosialhjelp.soknad.app.exceptions.Feilmelding
import no.nav.sosialhjelp.soknad.tilgangskontroll.XsrfGenerator
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
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
        token = mockOAuth2Server.issueToken("selvbetjening", userId, "someaudience", claims = mapOf("acr" to "idporten-loa-high"))
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

    protected fun doGetFullResponse(
        uri: String,
    ): ResponseSpec {
        return webTestClient.get()
            .uri(uri)
            .header("Authorization", "Bearer ${token.serialize()}")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
    }

    protected fun <T> doPost(
        uri: String,
        responseBodyClass: Class<T>,
        soknadId: UUID? = null,
    ): T {
        return webTestClient.post()
            .uri(uri)
            .header("Authorization", "Bearer ${token.serialize()}")
            .header("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken(soknadId?.toString(), id = token.jwtClaimsSet.subject))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(responseBodyClass)
            .returnResult()
            .responseBody!!
    }

    protected fun doPostFullResponse(
        uri: String,
    ): ResponseSpec {
        return webTestClient.post()
            .uri(uri)
            .header("Authorization", "Bearer ${token.serialize()}")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
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

    protected fun <T> doPostWithBody(
        uri: String,
        requestBody: Any,
        responseBodyClass: Class<T>,
        soknadId: UUID? = null,
    ): T {
        return webTestClient.post()
            .uri(uri)
            .header("Authorization", "Bearer ${token.serialize()}")
            .header("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken(soknadId?.toString(), id = token.jwtClaimsSet.subject))
            .contentType(MediaType.MULTIPART_FORM_DATA)
//            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(requestBody))
            .exchange()
            .expectStatus().isOk
            .expectBody(responseBodyClass)
            .returnResult()
            .responseBody!!
    }

    protected fun doPostFullResponse(
        uri: String,
        requestBody: Any,
        soknadId: UUID?,
    ): ResponseSpec {
        return webTestClient.post()
            .uri(uri)
            .header("Authorization", "Bearer ${token.serialize()}")
            .header("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken(soknadId?.toString(), id = token.jwtClaimsSet.subject))
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromValue(requestBody))
            .exchange()
    }

    protected fun doPutExpectError(
        uri: String,
        requestBody: Any,
        httpStatus: HttpStatus,
        soknadId: UUID? = null,
    ): Feilmelding {
        return webTestClient.put()
            .uri(uri)
            .header("Authorization", "Bearer ${token.serialize()}")
            .header("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken(soknadId?.toString(), id = token.jwtClaimsSet.subject))
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(requestBody))
            .exchange()
            .expectStatus().isEqualTo(httpStatus)
            .expectBody(Feilmelding::class.java)
            .returnResult()
            .responseBody!!
    }

    protected fun doDelete(
        uri: String,
        soknadId: UUID? = null,
    ): ResponseSpec {
        return webTestClient.delete()
            .uri(uri)
            .header("Authorization", "Bearer ${token.serialize()}")
            .header("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken(soknadId?.toString(), id = token.jwtClaimsSet.subject))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent
    }

    companion object {
        val userId = "05058548523"
    }
}
