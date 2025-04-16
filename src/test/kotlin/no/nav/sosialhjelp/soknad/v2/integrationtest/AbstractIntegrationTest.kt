package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.nimbusds.jwt.SignedJWT
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadApiError
import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import no.nav.sosialhjelp.soknad.v2.soknad.PersonIdService
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.web.reactive.function.BodyInserters
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "PT36000S")
@ActiveProfiles("no-redis", "test", "test-container")
abstract class AbstractIntegrationTest {
    @Autowired
    protected lateinit var webTestClient: WebTestClient

    @Autowired
    protected lateinit var soknadRepository: SoknadRepository

    @Autowired
    protected lateinit var eierRepository: EierRepository

    @Autowired
    protected lateinit var kontaktRepository: KontaktRepository

    @Autowired
    protected lateinit var soknadMetadataRepository: SoknadMetadataRepository

    @Autowired
    protected lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    protected lateinit var jdbcTemplate: JdbcTemplate

    @SpykBean
    protected lateinit var personIdService: PersonIdService

    protected lateinit var token: SignedJWT

    protected lateinit var soknadId: UUID

    @BeforeEach
    fun before() {
        soknadId = soknadMetadataRepository.save(opprettSoknadMetadata()).soknadId
        token = mockOAuth2Server.issueToken("selvbetjening", userId, "someaudience", claims = mapOf("acr" to "idporten-loa-high"))
        every { personIdService.findPersonId(soknadId) } returns userId
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
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(requestBody))
            .exchange()
            .expectStatus().isOk
            .expectBody(responseBodyClass)
            .returnResult()
            .responseBody!!
    }

    protected fun <T> doPost(
        uri: String,
        requestBody: Any,
        responseBodyClass: Class<T>,
        soknadId: UUID? = null,
    ): T {
        return webTestClient.post()
            .uri(uri)
            .header("Authorization", "Bearer ${token.serialize()}")
            .contentType(MediaType.APPLICATION_JSON)
//            .accept(MediaType.APPLICATION_JSON)
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
        contentType: MediaType = MediaType.APPLICATION_JSON,
    ): ResponseSpec {
        return webTestClient.post()
            .uri(uri)
            .header("Authorization", "Bearer ${token.serialize()}")
            .contentType(contentType)
            .body(BodyInserters.fromValue(requestBody))
            .exchange()
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
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(requestBody))
            .exchange()
            .expectStatus().isEqualTo(httpStatus)
            .expectBody(SoknadApiError::class.java)
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
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
    }

    companion object {
        val userId = "05058548523"
    }
}
