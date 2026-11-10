package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.api.minesaker.InnsendtSoknadDto
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.util.UUID

class MineSakerIntegrationTest : AbstractIntegrationTest(useTokenX = true) {
    @Autowired
    private lateinit var webClient: WebTestClient

    @Test
    fun `Hente mine innsendte saker skal kun returnere SENDTE og MOTTATTE`() {
        metadataRepository.deleteAll()

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

    private fun createMetadata(status: SoknadStatus): UUID {
        return SoknadMetadata(
            soknadId = UUID.randomUUID(),
            status = status,
            personId = USER_ID,
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
