package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sosialhjelp.soknad.api.minesaker.dto.InnsendtSoknadDto
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.Constants.SELVBETJENING
import no.nav.sosialhjelp.soknad.integrationtest.IntegrationTestUtils.issueToken
import no.nav.sosialhjelp.soknad.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

class MineSakerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var webClient: WebTestClient

    @BeforeEach
    override fun before() {
        useTokenX = true
        super.before()
    }

    @Test
    fun `Hente mine innsendte saker skal kun returnere SENDTE og MOTTATTE`() {
        listOf(
            createMetadata(SoknadStatus.MOTTATT_FSL),
            createMetadata(SoknadStatus.SENDT),
            createMetadata(SoknadStatus.OPPRETTET),
            createMetadata(SoknadStatus.INNSENDING_FEILET),
        )

        doGet(uri = url, responseBodyClass = String::class.java)
            .let { jacksonObjectMapper().readValue(it) as List<InnsendtSoknadDto> }
            .also { assertThat(it).hasSize(2) }
    }

    @Test
    internal fun innsendte_skalGi401UtenToken() {
        webClient
            .get()
            .uri(url)
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
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .header(
                HttpHeaders.AUTHORIZATION,
                BEARER +
                    issueToken(
                        mockOAuth2Server,
                        "11111111111",
                        issuer = SELVBETJENING,
                    ).serialize(),
            )
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    private fun createMetadata(status: SoknadStatus): UUID {
        return SoknadMetadata(
            soknadId = UUID.randomUUID(),
            status = status,
            personId = userId,
            digisosId = UUID.randomUUID(),
            mottakerKommunenummer = "0301",
            tidspunkt = Tidspunkt(sendtInn = nowWithMillis()),
        )
            .also { metadataRepository.save(it) }
            .soknadId
    }

    companion object {
        private val url get() = "/minesaker/innsendte"
    }
}
