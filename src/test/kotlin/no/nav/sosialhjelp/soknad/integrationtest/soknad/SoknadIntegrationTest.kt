package no.nav.sosialhjelp.soknad.integrationtest.soknad

import no.nav.sosialhjelp.soknad.app.exceptions.Feilmelding
import no.nav.sosialhjelp.soknad.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.util.*
import kotlin.jvm.optionals.getOrNull

class SoknadIntegrationTest : AbstractIntegrationTest() {

    @Test
    fun `Hente ut lagret soknad`() {

        val lagretSoknad = opprettSoknad()

        webTestClient
            .get()
            .uri("/soknad/${lagretSoknad.id}/hentSoknad")
            .accept(MediaType.APPLICATION_JSON)
//            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + token.serialize())
            .exchange()
            .expectStatus().isOk
            .expectBody(Soknad::class.java)
            .returnResult()
            .responseBody!!.run {
            assertThat(this).isEqualTo(lagretSoknad)
        }
    }

    @Test
    fun `Hente soknad som ikke finnes skal gi 404`() {

        webTestClient
            .get()
            .uri("/soknad/${UUID.randomUUID()}/hentSoknad")
            .accept(MediaType.APPLICATION_JSON)
//            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + token.serialize())
            .exchange()
            .expectStatus().isNotFound
            .expectBody(Feilmelding::class.java)
            .returnResult()
            .responseBody!!.run {
            assertThat(message).isEqualTo("Soknad finnes ikke")
        }
    }

    @Test
    fun `Slette lagret soknad`() {
        val lagretSoknadId = opprettSoknad().id
            ?: throw RuntimeException("Kunne ikke lagre soknad")

        webTestClient
            .delete()
            .uri("/soknad/$lagretSoknadId")
            .accept(MediaType.APPLICATION_JSON)
//            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + token.serialize())
            .exchange()
            .expectStatus().isNoContent
//            .expectBody(Soknad::class.java)
//            .returnResult().responseBody

        assertThat(soknadRepository.findById(lagretSoknadId).getOrNull()).isNull()
    }

    @Test
    fun `Slette soknad som ikke finnes skal gi 404`() {

        webTestClient
            .delete()
            .uri("/soknad/${UUID.randomUUID()}")
            .accept(MediaType.APPLICATION_JSON)
//            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + token.serialize())
            .exchange()
            .expectStatus().isNotFound
            .expectBody(Feilmelding::class.java)
            .returnResult()
            .responseBody!!.run {
            assertThat(message).isEqualTo("Soknad finnes ikke")
        }
    }
}
