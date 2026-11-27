package no.nav.sosialhjelp.soknad.app.subjecthandler

import no.nav.sosialhjelp.soknad.app.security.principal.CitizenPrincipal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class DefaultSubjectHandlerTest {
    @AfterEach
    fun cleanup() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `uses citizen principal for a validated idporten jwt`() {
        val jwt = jwt(IDPORTEN_ISSUER)
        val principal = CitizenPrincipal(jwt.tokenValue, jwt.claims, emptyList())
        SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(jwt, principal, emptyList())

        assertThat(DefaultSubjectHandler.getUserIdFromToken()).isEqualTo("test-person")
        assertThat(DefaultSubjectHandler.getToken()).isEqualTo(TEST_TOKEN)
    }

    @Test
    fun `rejects standard principal even with allowed issuer`() {
        val attributes = mapOf("iss" to TOKENX_ISSUER, "sub" to "test-subject", "pid" to "test-person")
        val principal = DefaultOAuth2AuthenticatedPrincipal(attributes, emptyList())
        val token = OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, TEST_TOKEN, null, null)
        SecurityContextHolder.getContext().authentication = BearerTokenAuthentication(principal, token, emptyList())

        assertNoUserAuthentication()
    }

    @Test
    fun `uses citizen principal in bearer authentication`() {
        val attributes = mapOf("iss" to TOKENX_ISSUER, "sub" to "test-subject")
        val principal = CitizenPrincipal(TEST_TOKEN, attributes, emptyList())
        val token = OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, TEST_TOKEN, null, null)
        SecurityContextHolder.getContext().authentication = BearerTokenAuthentication(principal, token, emptyList())

        assertThat(DefaultSubjectHandler.getUserIdFromToken()).isEqualTo("test-subject")
        assertThat(DefaultSubjectHandler.getToken()).isEqualTo(TEST_TOKEN)
    }

    @Test
    fun `rejects standard tokenx principal without identity`() {
        val principal = DefaultOAuth2AuthenticatedPrincipal(mapOf("iss" to TOKENX_ISSUER), emptyList())
        val token = OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, TEST_TOKEN, null, null)
        SecurityContextHolder.getContext().authentication = BearerTokenAuthentication(principal, token, emptyList())

        assertNoUserAuthentication()
    }

    @Test
    fun `uses citizen principal without checking issuer claim again`() {
        val attributes = mapOf("sub" to "test-subject")
        val principal = CitizenPrincipal(TEST_TOKEN, attributes, emptyList())
        val token = OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, TEST_TOKEN, null, null)
        SecurityContextHolder.getContext().authentication = BearerTokenAuthentication(principal, token, emptyList())

        assertThat(DefaultSubjectHandler.getUserIdFromToken()).isEqualTo("test-subject")
        assertThat(DefaultSubjectHandler.getToken()).isEqualTo(TEST_TOKEN)
    }

    @Test
    fun `rejects unauthenticated jwt and non-bearer authentication`() {
        SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(jwt(IDPORTEN_ISSUER))
        assertNoUserAuthentication()

        val jwt = jwt(IDPORTEN_ISSUER)
        val principal = CitizenPrincipal(jwt.tokenValue, jwt.claims, emptyList())
        val unauthenticated = JwtAuthenticationToken(jwt, principal, emptyList())
        unauthenticated.isAuthenticated = false
        SecurityContextHolder.getContext().authentication = unauthenticated
        assertNoUserAuthentication()

        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken.authenticated(principal, "credentials", emptyList())
        assertNoUserAuthentication()
    }

    @Test
    fun `handles missing authentication`() {
        assertNoUserAuthentication()
    }

    @Test
    fun `rejects entra principal`() {
        val attributes = mapOf("iss" to "https://entra.example", "sub" to "test-application")
        val principal = DefaultOAuth2AuthenticatedPrincipal(attributes, emptyList())
        val token = OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, TEST_TOKEN, null, null)
        SecurityContextHolder.getContext().authentication = BearerTokenAuthentication(principal, token, emptyList())

        assertNoUserAuthentication()
    }

    private fun assertNoUserAuthentication() {
        assertThrows<AuthenticationCredentialsNotFoundException> { DefaultSubjectHandler.getToken() }
        assertThrows<AuthenticationCredentialsNotFoundException> { DefaultSubjectHandler.getUserIdFromToken() }
    }

    private fun jwt(issuer: String): Jwt =
        Jwt.withTokenValue(TEST_TOKEN)
            .header("alg", "RS256")
            .claim("iss", issuer)
            .claim("pid", "test-person")
            .claim("sub", "test-subject")
            .build()

    companion object {
        private const val TEST_TOKEN = "test-token"
        private const val IDPORTEN_ISSUER = "https://idporten.example"
        private const val TOKENX_ISSUER = "https://tokenx.example"
    }
}
