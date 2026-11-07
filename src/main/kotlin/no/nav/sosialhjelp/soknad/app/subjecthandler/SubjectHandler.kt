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

    private fun requireAuthentication(): AbstractOAuth2TokenAuthenticationToken<*>? =
        (securityContext.authentication as? AbstractOAuth2TokenAuthenticationToken<*>)
            ?.takeIf { it.isAuthenticated }

    override fun getUserIdFromToken(): String =
        requireAuthentication()?.let {
            it.tokenAttributes["pid"] as? String
                ?: it.tokenAttributes["sub"] as? String
                ?: throw RuntimeException("Could not find any userId for token in pid or sub claim")
        }
            ?: throw AuthenticationCredentialsNotFoundException("No authenticated bearer token")

    override fun getToken(): String =
        requireAuthentication()?.token?.tokenValue
            ?: throw AuthenticationCredentialsNotFoundException("No authenticated bearer token")

    override fun getTokenOrNull(): String? = requireAuthentication()?.token?.tokenValue

    override fun getConsumerId(): String = "srvsoknadsosialhje"
}
