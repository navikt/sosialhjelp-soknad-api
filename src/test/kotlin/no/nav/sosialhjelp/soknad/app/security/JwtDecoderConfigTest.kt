package no.nav.sosialhjelp.soknad.app.security

import no.nav.security.mock.oauth2.MockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtValidators

class JwtDecoderConfigTest {
    @Test
    fun `decoder accepts configured issuer and rejects another issuer`() {
        val server = MockOAuth2Server()
        server.start()
        try {
            val decoder = JwtDecoderConfig(server.issuerUrl("idporten").toString(), "test-client").jwtDecoder()
            val claims = mapOf("client_id" to "test-client", "acr" to "idporten-loa-high")
            val validToken = server.issueToken(issuerId = "idporten", subject = "test-user", claims = claims)
            val wrongIssuer = server.issueToken(issuerId = "tokenx", subject = "test-user", claims = claims)

            assertThat(decoder.decode(validToken.serialize()).subject).isEqualTo("test-user")
            assertThrows<BadJwtException> { decoder.decode(wrongIssuer.serialize()) }

            val wrongIssuerClaims =
                Jwt.withTokenValue("test-token")
                    .header("alg", "RS256")
                    .claim("iss", server.issuerUrl("tokenx").toString())
                    .build()
            val issuerValidation = JwtValidators.createDefaultWithIssuer(server.issuerUrl("idporten").toString())
            assertThat(issuerValidation.validate(wrongIssuerClaims).errors.map { it.description })
                .anyMatch { it?.contains("iss claim", ignoreCase = true) == true }
        } finally {
            server.shutdown()
        }
    }
}
