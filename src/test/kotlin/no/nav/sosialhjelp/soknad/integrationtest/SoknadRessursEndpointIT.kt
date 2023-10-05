package no.nav.sosialhjelp.soknad.integrationtest

import jakarta.inject.Inject
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.integrationtest.IntegrationTestUtils.issueToken
import no.nav.sosialhjelp.soknad.integrationtest.IntegrationTestUtils.opprettSoknad
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@Import(PdlIntegrationTestConfig::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "PT30S")
@ActiveProfiles(profiles = ["no-redis", "test"])
class SoknadRessursEndpointIT {

    companion object {
        private const val BRUKER = "11111111111"
        private const val ANNEN_BRUKER = "22222222222"
    }

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    private lateinit var webClient: WebTestClient

    @Inject
    private lateinit var jdbcTemplate: JdbcTemplate

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update("delete from soknad_under_arbeid")
        jdbcTemplate.update("delete from soknadmetadata")
    }

    @Test
    internal fun nektetTilgang_opprettEttersendelse() {
        val behandlingsId = opprettSoknad(issueToken(mockOAuth2Server, BRUKER), webClient)

        webClient
            .post()
            .uri("/soknader/opprettSoknad?ettersendTil=$behandlingsId")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, BEARER + issueToken(mockOAuth2Server, ANNEN_BRUKER).serialize())
            .exchange()
            .expectStatus().isForbidden
    }
}
