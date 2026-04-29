package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.nimbusds.jwt.SignedJWT
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkSpyBean
import io.mockk.every
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.sosialhjelp.soknad.app.exceptions.InnsendingFeiletError
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadApiError
import no.nav.sosialhjelp.soknad.integrationtest.HentPersonClientMock
import no.nav.sosialhjelp.soknad.personalia.person.HentPersonClient
import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import no.nav.sosialhjelp.soknad.v2.soknad.PersonIdService
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
    protected lateinit var metadataRepository: SoknadMetadataRepository

    @Autowired
    protected lateinit var mockOAuth2Server: MockOAuth2Server

    @MockkSpyBean
    protected lateinit var personIdService: PersonIdService

    @MockkBean
    protected lateinit var hentPersonClient: HentPersonClient

    protected lateinit var token: SignedJWT

    protected lateinit var soknadId: UUID

    protected var opprettSoknadBeforeEach = true

    protected var useTokenX = false

    @BeforeEach
    fun before() {
        if (opprettSoknadBeforeEach) {
            soknadId = metadataRepository.save(opprettSoknadMetadata()).soknadId
            opprettSoknad(id = soknadId).also { soknadRepository.save(it) }
            every { personIdService.findPersonId(soknadId) } returns userId
        }

        setupPdlAnswers()

        token =
            when (useTokenX) {
                true -> Pair("tokenx", "localhost:teamdigisos:sosialhjelp-soknad-api")
                false -> Pair("selvbetjening", "someaudience")
            }.let { (issuer, audience) ->
                mockOAuth2Server.issueToken(
                    issuerId = issuer,
                    subject = userId,
                    audience = audience,
                    claims = mapOf("acr" to "idporten-loa-high"),
                )
            }
    }

    protected fun setupPdlAnswers() {
        every { hentPersonClient.hentPerson(any()) } returns HentPersonClientMock().hentPerson("ident")
        every { hentPersonClient.hentAdressebeskyttelse(any()) } returns HentPersonClientMock().hentAdressebeskyttelse("ident")
        every { hentPersonClient.hentEktefelle(any()) } returns HentPersonClientMock().hentEktefelle("ident")
        every { hentPersonClient.hentBarn(any()) } returns HentPersonClientMock().hentBarn("ident")
    }

    protected fun <T : Any> doGet(
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

    protected fun <T : Any> doPost(
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

    protected fun <T : Any> doPut(
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

    protected fun <T : Any> doPost(
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

    protected fun <T : Any> doPostWithBody(
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

    protected fun doPostExpectError(
        uri: String,
        requestBody: Any,
        httpStatus: HttpStatus,
        soknadId: UUID? = null,
    ): InnsendingFeiletError {
        return webTestClient.post()
            .uri(uri)
            .header("Authorization", "Bearer ${token.serialize()}")
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(requestBody))
            .exchange()
            .expectStatus().isEqualTo(httpStatus)
            .expectBody(InnsendingFeiletError::class.java)
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
