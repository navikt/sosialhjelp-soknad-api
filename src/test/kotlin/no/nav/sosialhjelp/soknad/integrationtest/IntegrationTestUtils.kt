package no.nav.sosialhjelp.soknad.integrationtest

import com.nimbusds.jwt.SignedJWT
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.sosialhjelp.soknad.app.Constants.SELVBETJENING

object IntegrationTestUtils {
    fun issueToken(
        mockOAuth2Server: MockOAuth2Server,
        fnr: String,
        issuer: String = SELVBETJENING,
        audience: String = "someaudience",
        claims: Map<String, Any> = mapOf("acr" to "Level4"),
        expiry: Long = 60L,
    ): SignedJWT {
        return mockOAuth2Server.issueToken(issuer, fnr, audience, claims, expiry)
    }
}
