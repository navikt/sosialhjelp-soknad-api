package no.nav.sosialhjelp.soknad.app.subjecthandler

import no.nav.sosialhjelp.soknad.app.security.principal.CitizenPrincipal
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken

interface SubjectHandler {
    fun getConsumerId(): String

    fun getUserIdFromToken(): String

    fun getToken(): String
}

object DefaultSubjectHandler : SubjectHandler {
    private val securityContext: SecurityContext get() = SecurityContextHolder.getContext()

    private fun requireUserPrincipal(): CitizenPrincipal =
        (securityContext.authentication as? AbstractOAuth2TokenAuthenticationToken<*>)
            ?.let { auth -> if (auth.isAuthenticated) auth else null }
            ?.let { auth -> auth.principal as? CitizenPrincipal }
            ?: throw AuthenticationCredentialsNotFoundException("No authenticated bearer token from TokenX or ID-porten")

    override fun getUserIdFromToken(): String = requireUserPrincipal().userId

    override fun getToken(): String = requireUserPrincipal().token

    override fun getConsumerId(): String = "srvsoknadsosialhje"
}
