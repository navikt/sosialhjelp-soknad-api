package no.nav.sosialhjelp.soknad.integrationtest.soknad

import no.nav.sosialhjelp.soknad.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class SoknadIntegrationTest: AbstractIntegrationTest() {

    @Test
    fun `Hente ut lagret soknad`() {

        val lagretSoknad = opprettSoknad()

        val hentetSoknad = webTestClient
            .get()
            .uri("/soknad/${lagretSoknad.id}/hentSoknad")
            .accept(MediaType.APPLICATION_JSON)
//            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + token.serialize())
            .exchange()
            .expectStatus().isOk
            .expectBody(Soknad::class.java)
            .returnResult().responseBody

        Assertions.assertThat(lagretSoknad).isEqualTo(hentetSoknad)
    }
}
