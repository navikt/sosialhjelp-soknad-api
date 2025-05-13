package no.nav.sosialhjelp.soknad.v2.interceptor

import com.nimbusds.jwt.SignedJWT
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadApiError
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.personalia.person.HentPersonClient
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.dto.AdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering.STRENGT_FORTROLIG
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering.UGRADERT
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonAdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest.Companion.userId
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "PT36000S")
@ActiveProfiles("no-redis", "test", "test-container")
class AdressebeskyttelseInterceptorTest {
    @Autowired
    private lateinit var soknadService: SoknadService

    @Autowired
    private lateinit var soknadMetadataRepository: SoknadMetadataRepository

    @Autowired
    private lateinit var soknadRepository: SoknadRepository

    @Autowired
    private lateinit var webClient: WebTestClient

    @SpykBean
    private lateinit var personService: PersonService

    @MockkBean
    private lateinit var hentPersonClient: HentPersonClient

    @SpykBean
    private lateinit var adressebeskyttelseInterceptor: AdressebeskyttelseInterceptor

    @Autowired
    protected lateinit var mockOAuth2Server: MockOAuth2Server

    protected lateinit var token: SignedJWT

    protected lateinit var soknadId: UUID

    @BeforeEach
    fun before() {
        every { hentPersonClient.hentAdressebeskyttelse(any()) } returns createAdressebeskyttelseDto(STRENGT_FORTROLIG)
        token = mockOAuth2Server.issueToken("selvbetjening", userId, "someaudience", claims = mapOf("acr" to "idporten-loa-high"))
    }

    @AfterEach
    fun teardown() {
        verify(exactly = 1) { adressebeskyttelseInterceptor.preHandle(any(), any(), any()) }
        clearAllMocks()
    }

    @Test
    fun `Kall til Session skal ikke kaste exception ved adressebeskyttelse`() {
        webClient.doGetFullReponse(
            uri = "/informasjon/session",
            token = token,
        )
            .expectStatus().isForbidden
            .expectBody(SoknadApiError::class.java)
            .returnResult().responseBody
            .also { assertThat(it?.error?.name).isEqualTo(HttpStatus.FORBIDDEN.reasonPhrase) }

        verify(exactly = 0) { personService.onSendSoknadHasAdressebeskyttelse(any()) }
        verify(exactly = 1) { personService.hasAdressebeskyttelse(any()) }
    }

    @Test
    fun `Kall til et annet endepunkt med adressebeskyttelse skal kaste exception`() {
        webClient.doPostFullResponse(
            uri = "/soknad/create",
            token = token,
        )
            .expectStatus().isForbidden
            .expectBody(SoknadApiError::class.java)
            .returnResult().responseBody
            .also { soknadApiError ->
                assertThat(soknadApiError?.error?.name).isEqualTo(HttpStatus.FORBIDDEN.reasonPhrase)
            }

        verify(exactly = 0) { personService.onSendSoknadHasAdressebeskyttelse(any()) }
        verify(exactly = 1) { personService.hasAdressebeskyttelse(any()) }
    }

    @Test
    fun `Kall til send skal ikke hente fra cache`() {
        val uuid = UUID.randomUUID()

        webClient.doPostFullResponse(
            uri = "/soknad/$uuid/send",
            soknadId = uuid,
            token = token,
        )
            .expectStatus().isForbidden
            .expectBody(SoknadApiError::class.java)
            .returnResult().responseBody
            .also { soknadApiError ->
                assertThat(soknadApiError?.error?.name).isEqualTo(HttpStatus.FORBIDDEN.reasonPhrase)
            }

        verify(exactly = 1) { personService.onSendSoknadHasAdressebeskyttelse(any()) }
        verify(exactly = 0) { personService.hasAdressebeskyttelse(any()) }
    }

    @Test
    fun `HasAdressebeskyttelse skal fjerne eksisterende soknader`() {
        mockkObject(SubjectHandlerUtils)

        val metadata =
            opprettSoknadMetadata()
                .also { metadata -> soknadMetadataRepository.save(metadata) }
                .let { metadata -> opprettSoknad(id = metadata.soknadId) }
                .also { soknad -> soknadRepository.save(soknad) }

        every { SubjectHandlerUtils.getUserIdFromToken() } returns metadata.eierPersonId

        soknadService.findOpenSoknadIds(metadata.eierPersonId).also { assertThat(it).hasSize(1) }

        webClient.doGetFullReponse(
            uri = "/informasjon/session",
            token = token,
        )
            .expectStatus().isForbidden
            .expectBody(SoknadApiError::class.java)
            .returnResult().responseBody
            .also { assertThat(it?.error?.name).isEqualTo(HttpStatus.FORBIDDEN.reasonPhrase) }

        soknadService.findOpenSoknadIds(metadata.eierPersonId).also { assertThat(it).isEmpty() }
        soknadMetadataRepository.findAll().also { assertThat(it).isEmpty() }

        verify(exactly = 0) { personService.onSendSoknadHasAdressebeskyttelse(any()) }
        verify(exactly = 1) { personService.hasAdressebeskyttelse(any()) }

        unmockkObject(SubjectHandlerUtils)
    }
}

private fun createAdressebeskyttelseDto(gradering: Gradering = UGRADERT): PersonAdressebeskyttelseDto =
    PersonAdressebeskyttelseDto(listOf(AdressebeskyttelseDto(gradering)))

private fun WebTestClient.doGetFullReponse(
    uri: String,
    token: SignedJWT,
): WebTestClient.ResponseSpec =
    get()
        .uri(uri)
        .header("Authorization", "Bearer ${token.serialize()}")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()

private fun WebTestClient.doPostFullResponse(
    uri: String,
    soknadId: UUID? = null,
    token: SignedJWT,
): WebTestClient.ResponseSpec {
    return post()
        .uri(uri)
        .header("Authorization", "Bearer ${token.serialize()}")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
}
