package no.nav.sosialhjelp.soknad.app.subjecthandler

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken

interface SubjectHandler {
    fun getConsumerId(): String

    fun getUserIdFromToken(): String

    fun getToken(): String

    fun getTokenOrNull(): String?
}

object DefaultSubjectHandler : SubjectHandler {
    private val securityContext: SecurityContext get() = SecurityContextHolder.getContext()
    private const val TOKEN_X_ISSUER = "TOKEN_X_ISSUER"
    private const val IDPORTEN_ISSUER = "IDPORTEN_ISSUER"

    internal val allowedIssuers: Set<String> by lazy {
        setOf(TOKEN_X_ISSUER, IDPORTEN_ISSUER)
            .map { name ->
                checkNotNull(System.getenv(name)?.takeIf { it.isNotBlank() }) {
                    "Missing environment variable $name"
                }
            }.toSet()
    }

    private fun userAuthenticationOrNull(): AbstractOAuth2TokenAuthenticationToken<*>? {
        val authentication =
            securityContext.authentication as? AbstractOAuth2TokenAuthenticationToken<*>
                ?: return null

        if (!authentication.isAuthenticated) return null

        val issuer =
            authentication.tokenAttributes["iss"]?.toString()
                ?: return null

        return authentication.takeIf { issuer in allowedIssuers }
    }

    private fun requireUserAuthentication(): AbstractOAuth2TokenAuthenticationToken<*> =
        userAuthenticationOrNull()
            ?: throw AuthenticationCredentialsNotFoundException(
                "No authenticated bearer token from TokenX or ID-porten",
            )

    override fun getUserIdFromToken(): String =
        requireUserAuthentication().let {
            it.tokenAttributes["pid"] as? String
                ?: it.tokenAttributes["sub"] as? String
                ?: throw IllegalStateException("Could not find any userId for token in pid or sub claim")
        }

    override fun getToken(): String = requireUserAuthentication().token.tokenValue

    override fun getTokenOrNull(): String? = userAuthenticationOrNull()?.token?.tokenValue

    override fun getConsumerId(): String = "srvsoknadsosialhje"
}
