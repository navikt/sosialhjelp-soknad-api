package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.nimbusds.jwt.SignedJWT
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkSpyBean
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.sosialhjelp.soknad.MockOAuth2Initializer
import no.nav.sosialhjelp.soknad.app.exceptions.InnsendingFeiletError
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadApiError
import no.nav.sosialhjelp.soknad.app.security.texas.TexasClient
import no.nav.sosialhjelp.soknad.app.security.texas.TokenResponse
import no.nav.sosialhjelp.soknad.integrationtest.HentPersonClientMock
import no.nav.sosialhjelp.soknad.personalia.person.HentPersonClient
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.UploadClient
import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import no.nav.sosialhjelp.soknad.v2.soknad.PersonIdService
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.web.reactive.function.BodyInserters
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "PT36000S")
@ContextConfiguration(initializers = [MockOAuth2Initializer::class])
@ActiveProfiles("no-redis", "test", "test-container")
abstract class AbstractIntegrationTest(protected var useTokenX: Boolean = false) {
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

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    protected lateinit var mockOAuth2Server: MockOAuth2Server

    @MockkSpyBean
    protected lateinit var personIdService: PersonIdService

    @MockkSpyBean
    protected lateinit var hentPersonClient: HentPersonClient

    @MockkBean
    protected lateinit var uploadClient: UploadClient

    @MockkBean
    protected lateinit var texasClient: TexasClient

    @Value("\${idporten.audience}")
    protected lateinit var idportenAudience: String

    protected lateinit var token: SignedJWT

    protected lateinit var soknadId: UUID

    protected var opprettSoknadBeforeEach = true

    @BeforeEach
    fun before() {
        if (opprettSoknadBeforeEach) {
            soknadId = metadataRepository.save(opprettSoknadMetadata()).soknadId
            opprettSoknad(id = soknadId).also { soknadRepository.save(it) }
            every { personIdService.findPersonId(soknadId) } returns USER_ID
        }

        setupTexasAnswer()
        setupPdlAnswers()

        every { uploadClient.getVedleggSpesifikasjon(any()) } returns JsonVedleggSpesifikasjon()
        every { uploadClient.delete(any(), any<UUID>()) } just runs

        token =
            when (useTokenX) {
                true -> Pair("tokenx", "localhost:teamdigisos:sosialhjelp-soknad-api")
                false -> Pair("idporten", idportenAudience)
            }.let { (issuer, audience) ->
                mockOAuth2Server.issueToken(
                    issuerId = issuer,
                    subject = USER_ID,
                    audience = audience,
                    claims =
                        mapOf(
                            "acr" to "idporten-loa-high",
                            "client_id" to audience,
                        ),
                )
            }
    }

    protected fun setupTexasAnswer() {
        every { texasClient.validateToken(any(), any()) } returns
            TokenResponse.ValidationResponse(active = true)
                .apply {
                    claims.putAll(
                        mapOf(
                            "acr" to "idporten-loa-high",
                            "sub" to USER_ID,
                        ),
                    )
                }
    }

    protected fun setupPdlAnswers() {
        coEvery { hentPersonClient.hentPerson(any()) } coAnswers { HentPersonClientMock().hentPerson("ident") }
        coEvery { hentPersonClient.hentAdressebeskyttelse() } coAnswers { HentPersonClientMock().hentAdressebeskyttelse() }
        coEvery { hentPersonClient.hentEktefelle(any()) } coAnswers { HentPersonClientMock().hentEktefelle("ident") }
        coEvery { hentPersonClient.hentBarn(any()) } coAnswers { HentPersonClientMock().hentBarn("ident") }
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
    ): T {
        return webTestClient.post()
            .uri(uri)
            .header("Authorization", "Bearer ${token.serialize()}")
            .contentType(MediaType.MULTIPART_FORM_DATA)
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
    ): ResponseSpec {
        return webTestClient.delete()
            .uri(uri)
            .header("Authorization", "Bearer ${token.serialize()}")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
    }

    protected fun MockOAuth2Server.issueIdPortenToken(subject: String? = null) =
        issueToken(
            issuerId = "idporten",
            subject = subject ?: USER_ID,
            audience = idportenAudience,
            claims =
                mapOf(
                    "acr" to "idporten-loa-high",
                    "client_id" to idportenAudience,
                ),
        )

    companion object {
        const val USER_ID = "05058548523"
    }
}
