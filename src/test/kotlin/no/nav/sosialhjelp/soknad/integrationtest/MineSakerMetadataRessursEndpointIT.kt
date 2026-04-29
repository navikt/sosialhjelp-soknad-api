package no.nav.sosialhjelp.soknad.integrationtest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.Constants.SELVBETJENING
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
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
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "PT30S")
@ActiveProfiles(profiles = ["no-redis", "test", "test-container"])
class MineSakerMetadataRessursEndpointIT {
    companion object {
        private const val BRUKER = "11111111111"
    }

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    private lateinit var webClient: WebTestClient

    @MockkBean
    private lateinit var texasService: TexasService

    @MockkBean
    private lateinit var hentPersonClient: HentPersonClient

    @BeforeEach
    fun setup() {
        every { hentPersonClient.hentAdressebeskyttelse(any()) } returns
            HentPersonClientMock().hentAdressebeskyttelse("ident")

        every { texasService.getToken(any(), any()) } returns
            issueToken(mockOAuth2Server, BRUKER, issuer = SELVBETJENING).serialize()
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
    }

    @Test
    internal fun innsendte_skalGi401MedAnnenIssuer() {
        // Skal kun godta tokenx som issuer
        webClient
            .get()
            .uri("/minesaker/innsendte")
            .accept(MediaType.APPLICATION_JSON)
            .header(
                HttpHeaders.AUTHORIZATION,
                BEARER + issueToken(mockOAuth2Server, BRUKER, issuer = SELVBETJENING).serialize(),
            )
            .exchange()
            .expectStatus()
            .isUnauthorized
    }
}
