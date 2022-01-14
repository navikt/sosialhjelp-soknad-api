package no.nav.sosialhjelp.soknad.integrationtest

import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.PabegynteSoknaderRespons
import no.nav.sosialhjelp.client.utils.Constants.BEARER
import no.nav.sosialhjelp.soknad.Application
import no.nav.sosialhjelp.soknad.integrationtest.IntegrationTestUtils.opprettSoknad
import no.nav.sosialhjelp.soknad.web.oidc.JwtTokenGenerator
import no.nav.sosialhjelp.soknad.web.oidc.OidcConfig
import org.assertj.core.api.Assertions.assertThat
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
class SaksoversiktMetadataOidcRessursEndpointIT {

    val BRUKER = "11111111111"
    val ANNEN_BRUKER = "22222222222"

    @Test
    internal fun innsendte_skalGi401UtenToken(@Autowired webClient: WebTestClient) {
        opprettSoknad(BRUKER, webClient)

        webClient
            .get().uri("/metadata/oidc/innsendte")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    internal fun ettersendelse_skalGi401UtenToken(@Autowired webClient: WebTestClient) {
        opprettSoknad(BRUKER, webClient)

        webClient
            .get().uri("/metadata/oidc/ettersendelse")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    internal fun pabegynte_skalGi401UtenToken(@Autowired webClient: WebTestClient) {
        opprettSoknad(BRUKER, webClient)

        webClient
            .get().uri("/metadata/oidc/pabegynte")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    internal fun skalIkkeSePabegynteForAnnenBruker(@Autowired webClient: WebTestClient) {
        opprettSoknad(BRUKER, webClient)

        val body = webClient
            .get().uri("/metadata/oidc/pabegynte")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, BEARER + JwtTokenGenerator.createSignedJWT(ANNEN_BRUKER).serialize())
            .exchange()
            .expectStatus().isOk
            .expectBody(PabegynteSoknaderRespons::class.java)
            .returnResult().responseBody

        assertThat(body?.pabegynteSoknader).isEmpty()
    }

    @Test
    internal fun ping_skalGi200UtenToken(@Autowired webClient: WebTestClient) {
        opprettSoknad(BRUKER, webClient)

        webClient
            .get().uri("/metadata/oidc/ping")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
    }
}
