package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import no.nav.sosialhjelp.soknad.app.exceptions.Feilmelding
import no.nav.sosialhjelp.soknad.tilgangskontroll.XsrfGenerator
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

class SoknadIntegrationTest : AbstractIntegrationTest() {
    @MockkBean
    private lateinit var mellomlagringClient: MellomlagringClient

    @BeforeEach
    fun setup() {
        every { mellomlagringClient.deleteAllVedlegg(any()) } just runs
    }

    @Test
    fun `Skal slette lagret soknad`() {
        val lagretSoknadId = opprettSoknad().let { soknadRepository.save(it).id }

        webTestClient
            .delete()
            .uri("/soknad/$lagretSoknadId")
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "BEARER ${token.serialize()}")
            .header("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken(lagretSoknadId.toString(), id = token.jwtClaimsSet.subject))
            .exchange()
            .expectStatus().isNoContent

        assertThat(soknadRepository.findById(lagretSoknadId).getOrNull()).isNull()
    }

    @Test
    fun `Slette soknad som ikke finnes skal gi 404`() {
        val randomUUID = UUID.randomUUID()
        webTestClient
            .delete()
            .uri("/soknad/$randomUUID")
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "BEARER ${token.serialize()}")
            .header("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken(randomUUID.toString(), id = token.jwtClaimsSet.subject))
            .exchange()
            .expectStatus().isNotFound
            .expectBody(Feilmelding::class.java)
            .returnResult()
            .responseBody!!.also {
            assertThat(it.message).isEqualTo("NyModell: Soknad finnes ikke")
        }
    }
}
