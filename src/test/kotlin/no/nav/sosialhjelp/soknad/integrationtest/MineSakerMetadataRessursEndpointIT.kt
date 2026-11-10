package no.nav.sosialhjelp.soknad.integrationtest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.every
import io.mockk.verify
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.sosialhjelp.soknad.MockOAuth2Initializer
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.security.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.app.security.texas.TexasClient
import no.nav.sosialhjelp.soknad.app.security.texas.TokenResponse
import no.nav.sosialhjelp.soknad.integrationtest.IntegrationTestUtils.issueToken
import no.nav.sosialhjelp.soknad.personalia.person.HentPersonClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "PT30S")
@ContextConfiguration(initializers = [MockOAuth2Initializer::class])
@ActiveProfiles(profiles = ["no-redis", "test", "test-container"])
class MineSakerMetadataRessursEndpointIT {
    companion object {
        private const val BRUKER = "11111111111"
    }

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    protected lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    private lateinit var webClient: WebTestClient

    @MockkBean
    private lateinit var texasClient: TexasClient

    @MockkBean
    private lateinit var hentPersonClient: HentPersonClient

    @BeforeEach
    suspend fun setup() {
        coEvery { hentPersonClient.hentAdressebeskyttelse() } returns HentPersonClientMock().hentAdressebeskyttelse()
    }

    @Test
    internal fun innsendte_skalGi401UtenToken() {
        webClient
            .get()
            .uri("/minesaker/innsendte")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isUnauthorized

        verify(exactly = 0) { texasClient.validateToken(any(), any()) }
    }

    @Test
    internal fun innsendte_skalGi401MedAnnenIssuer() {
        every { texasClient.validateToken(IdentityProvider.TOKENX.value, any()) } returns TokenResponse.ValidationResponse(false)

        // Skal kun godta tokenx som issuer
        webClient
            .get()
            .uri("/minesaker/innsendte")
            .accept(MediaType.APPLICATION_JSON)
            .header(
                HttpHeaders.AUTHORIZATION,
                BEARER + issueToken(mockOAuth2Server, BRUKER, issuer = IdentityProvider.IDPORTEN.value).serialize(),
            )
            .exchange()
            .expectStatus()
            .isUnauthorized
            .expectHeader()
            .valueMatches(
                HttpHeaders.WWW_AUTHENTICATE,
                """Bearer .*error="invalid_token".*""",
            )
    }
}
