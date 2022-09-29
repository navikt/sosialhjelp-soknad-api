package no.nav.sosialhjelp.soknad.integrationtest

import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.sosialhjelp.soknad.Application
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.integrationtest.IntegrationTestUtils.issueToken
import no.nav.sosialhjelp.soknad.integrationtest.IntegrationTestUtils.opprettSoknad
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient

@ContextConfiguration(classes = [IntegrationTestConfig::class, PdlIntegrationTestConfig::class])
@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = ["no-redis", "test"])
class SoknadActionsEndpointIT {

    companion object {
        private const val BRUKER = "11111111111"
        private const val ANNEN_BRUKER = "22222222222"
    }

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    private lateinit var webClient: WebTestClient

    @Test
    internal fun sendSoknad_skalGiForbiddenMedAnnenBruker() {
        val behandlingsId = opprettSoknad(issueToken(mockOAuth2Server, BRUKER), webClient)

        webClient
            .post().uri("/soknader/$behandlingsId/actions/send")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, BEARER + issueToken(mockOAuth2Server, ANNEN_BRUKER).serialize())
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    internal fun sendSoknad_skalGi401UtenToken() {
        val behandlingsId = opprettSoknad(issueToken(mockOAuth2Server, BRUKER), webClient)

        webClient
            .post().uri("/soknader/$behandlingsId/actions/send")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }
}
