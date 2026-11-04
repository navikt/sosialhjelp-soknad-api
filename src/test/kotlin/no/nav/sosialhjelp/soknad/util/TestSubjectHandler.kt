package no.nav.sosialhjelp.soknad.util

import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandler
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication

object TestSubjectHandler : SubjectHandler {

    private const val DEFAULT_USER = "11111111111"
    private const val DEFAULT_TOKEN = "test token"

    private var user = createPrincipal(DEFAULT_USER)
    private var token = createBearerToken(DEFAULT_TOKEN)

    override fun getUserIdFromToken(): String =
        user.attributes["pid"] as? String
            ?: user.attributes["sub"] as? String
            ?: throw RuntimeException("Could not find any userId for token in sub claim")

    override fun getToken(): String = token.tokenValue

    override fun getTokenOrNull(): String = token.tokenValue

    override fun getConsumerId(): String = "StaticConsumerId"

    fun setUser(username: String) {
        user = createPrincipal(username)
        createSecurityContext(createAuthentication(user))
    }

    private fun createPrincipal(username: String): DefaultOAuth2AuthenticatedPrincipal {
        return DefaultOAuth2AuthenticatedPrincipal(
            mapOf<String, Any>("sub" to username),
            emptyList(),
        )
    }

    private fun createBearerToken(tokenValue: String): OAuth2AccessToken {
        return OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            tokenValue,
            null,
            null,
        )
    }

    private fun createAuthentication(
        principal: DefaultOAuth2AuthenticatedPrincipal = this.user,
        token: OAuth2AccessToken = this.token,
    ): BearerTokenAuthentication {
        return BearerTokenAuthentication(
            principal,
            token,
            principal.authorities,
        )
    }

    private fun createSecurityContext(authentication: BearerTokenAuthentication = createAuthentication()) {
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication
        SecurityContextHolder.setContext(context)
    }
}
