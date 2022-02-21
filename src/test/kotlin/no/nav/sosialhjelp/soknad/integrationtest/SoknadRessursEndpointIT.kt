package no.nav.sosialhjelp.soknad.integrationtest

import no.nav.sosialhjelp.client.utils.Constants.BEARER
import no.nav.sosialhjelp.soknad.Application
import no.nav.sosialhjelp.soknad.integrationtest.IntegrationTestUtils.opprettSoknad
import no.nav.sosialhjelp.soknad.integrationtest.oidc.JwtTokenGenerator
import no.nav.sosialhjelp.soknad.integrationtest.oidc.OidcConfig
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient

@ContextConfiguration(classes = [OidcConfig::class, PdlIntegrationTestConfig::class])
@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = ["no-redis", "test"])
class SoknadRessursEndpointIT {

    private val BRUKER = "11111111111"
    private val ANNEN_BRUKER = "22222222222"

    @Test
    internal fun nektetTilgang_opprettEttersendelse(@Autowired webClient: WebTestClient) {
        val behandlingsId = opprettSoknad(BRUKER, webClient)

        webClient
            .post()
            .uri("/soknader/opprettSoknad?ettersendTil=$behandlingsId")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, BEARER + JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER).serialize())
            .exchange()
            .expectStatus().isForbidden
    }
}
