package no.nav.sosialhjelp.soknad.integrationtest

import no.nav.sosialhjelp.client.utils.Constants
import no.nav.sosialhjelp.soknad.web.oidc.JwtTokenGenerator
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

object IntegrationTestUtils {

    fun opprettSoknad(fnr: String, webClient: WebTestClient): String? {
        val body = webClient
            .post()
            .uri("/soknader/opprettSoknad")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, Constants.BEARER + JwtTokenGenerator.createSignedJWT(fnr).serialize())
            .exchange()
            .expectStatus().isOk
            .expectBody(Map::class.java)
            .returnResult().responseBody

        return body["brukerBehandlingId"] as? String
    }
}
