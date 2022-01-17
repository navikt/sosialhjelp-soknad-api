package no.nav.sosialhjelp.soknad.integrationtest

import no.nav.sosialhjelp.client.utils.Constants.BEARER
import no.nav.sosialhjelp.soknad.Application
import no.nav.sosialhjelp.soknad.integrationtest.IntegrationTestUtils.opprettSoknad
import no.nav.sosialhjelp.soknad.web.oidc.JwtTokenGenerator
import no.nav.sosialhjelp.soknad.web.oidc.OidcConfig
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
class MineSakerMetadataRessursEndpointIT {

    val BRUKER = "11111111111"

    @Test
    internal fun innsendte_skalGi401UtenToken(@Autowired webClient: WebTestClient) {
        opprettSoknad(BRUKER, webClient)

        webClient
            .get().uri("/minesaker/innsendte")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    internal fun innsendte_skalGi401MedAnnenIssuer(@Autowired webClient: WebTestClient) {
        opprettSoknad(BRUKER, webClient)

        webClient
            .get().uri("/minesaker/innsendte")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, BEARER + JwtTokenGenerator.createSignedJWT(BRUKER).serialize())
            .exchange()
            .expectStatus().isUnauthorized
    }
}
