package no.nav.sosialhjelp.soknad.integrationtest

import com.nimbusds.jwt.SignedJWT
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.sosialhjelp.soknad.common.Constants.BEARER
import no.nav.sosialhjelp.soknad.common.Constants.SELVBETJENING
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

object IntegrationTestUtils {

    fun opprettSoknad(token: SignedJWT, webClient: WebTestClient): String? {
        val body = webClient
            .post()
            .uri("/soknader/opprettSoknad")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, BEARER + token.serialize())
            .exchange()
            .expectStatus().isOk
            .expectBody(Map::class.java)
            .returnResult().responseBody

        return body["brukerBehandlingId"] as? String
    }

    fun issueToken(
        mockOAuth2Server: MockOAuth2Server,
        fnr: String,
        issuer: String = SELVBETJENING,
        audience: String = "someaudience",
        claims: Map<String, Any> = mapOf("acr" to "Level4"),
        expiry: Long = 60L
    ): SignedJWT {
        return mockOAuth2Server.issueToken(issuer, fnr, audience, claims, expiry)
    }
}
