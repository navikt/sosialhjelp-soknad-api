package no.nav.sosialhjelp.soknad.integrationtest

import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.PabegynteSoknaderRespons
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.sosialhjelp.soknad.Application
import no.nav.sosialhjelp.soknad.common.Constants.BEARER
import no.nav.sosialhjelp.soknad.integrationtest.IntegrationTestUtils.issueToken
import no.nav.sosialhjelp.soknad.integrationtest.IntegrationTestUtils.opprettSoknad
import org.assertj.core.api.Assertions.assertThat
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
class SaksoversiktMetadataOidcRessursEndpointIT {

    val BRUKER = "11111111111"
    val ANNEN_BRUKER = "22222222222"

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    private lateinit var webClient: WebTestClient

    @Test
    internal fun innsendte_skalGi401UtenToken() {
        opprettSoknad(issueToken(mockOAuth2Server, BRUKER), webClient)

        webClient
            .get().uri("/metadata/oidc/innsendte")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    internal fun ettersendelse_skalGi401UtenToken() {
        opprettSoknad(issueToken(mockOAuth2Server, BRUKER), webClient)

        webClient
            .get().uri("/metadata/oidc/ettersendelse")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    internal fun pabegynte_skalGi401UtenToken() {
        opprettSoknad(issueToken(mockOAuth2Server, BRUKER), webClient)

        webClient
            .get().uri("/metadata/oidc/pabegynte")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    internal fun skalIkkeSePabegynteForAnnenBruker() {
        opprettSoknad(issueToken(mockOAuth2Server, BRUKER), webClient)

        val body = webClient
            .get().uri("/metadata/oidc/pabegynte")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, BEARER + issueToken(mockOAuth2Server, ANNEN_BRUKER).serialize())
            .exchange()
            .expectStatus().isOk
            .expectBody(PabegynteSoknaderRespons::class.java)
            .returnResult().responseBody

        assertThat(body?.pabegynteSoknader).isEmpty()
    }

    @Test
    internal fun ping_skalGi200UtenToken() {
        opprettSoknad(issueToken(mockOAuth2Server, BRUKER), webClient)

        webClient
            .get().uri("/metadata/oidc/ping")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
    }
}
