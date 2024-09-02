package no.nav.sosialhjelp.soknad.integrationtest

import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadServiceOld
import no.nav.sosialhjelp.soknad.integrationtest.IntegrationTestUtils.issueToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "PT30S")
@ActiveProfiles(profiles = ["no-redis", "test", "test-container"])
class SoknadActionsEndpointIT {
    companion object {
        private const val BRUKER = "11111111111"
        private const val ANNEN_BRUKER = "22222222222"
    }

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    private lateinit var webClient: WebTestClient

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var soknadUnderArbeidRepository: SoknadUnderArbeidRepository

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update("delete from soknad_under_arbeid")
    }

    @Test
    internal fun sendSoknad_skalGiForbiddenMedAnnenBruker() {
        val behandlingsId = opprettSoknad()

        webClient
            .post()
            .uri("/soknader/$behandlingsId/actions/send")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, BEARER + issueToken(mockOAuth2Server, ANNEN_BRUKER).serialize())
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    internal fun sendSoknad_skalGi401UtenToken() {
        val behandlingsId = opprettSoknad()

        webClient
            .post()
            .uri("/soknader/$behandlingsId/actions/send")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    private fun opprettSoknad(): String {
        return SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = "BEHANDLINGSID",
            eier = BRUKER,
            jsonInternalSoknad =
                SoknadServiceOld.createEmptyJsonInternalSoknad(
                    BRUKER,
                    false,
                ),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now(),
        )
            .also { soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid = it, eier = BRUKER) }
            .behandlingsId
    }
}
