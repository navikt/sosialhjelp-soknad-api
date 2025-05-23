package no.nav.sosialhjelp.soknad.v2.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.config.SoknadApiHandlerInterceptor
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadApiErrorType
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class AdressebeskyttelseInterceptor(
    private val personService: PersonService,
    private val metadataService: SoknadMetadataService,
    private val soknadService: SoknadService,
) : SoknadApiHandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (isUriUnprotected(request.requestURI)) return true
        // finnes det ingen auth header, får vi heller ikke sjekket adressebeskyttelse
        authorization()?.also { if (hasAdressebeskyttelse(request.requestURI)) handleHasAdressebeskyttelse() }
        return true
    }

    private fun isUriUnprotected(requestURI: String): Boolean = FILTERED_URIS.any { requestURI.contains(it) }

    private fun authorization() =
        runCatching { SubjectHandlerUtils.getTokenOrNull() }
            .onFailure { logger.warn("Feil ved henting av token", it) }
            .getOrNull()

    // hente ikke fra potensiell cache ved sending av soknad
    private fun hasAdressebeskyttelse(uri: String): Boolean =
        when (uri.contains("/send")) {
            true -> personService.onSendSoknadHasAdressebeskyttelse(getUserIdFromToken())
            false -> personService.hasAdressebeskyttelse(getUserIdFromToken())
        }

    private fun handleHasAdressebeskyttelse() {
        soknadService.findOpenSoknadIds(getUserIdFromToken())
            .takeIf { it.isNotEmpty() }
            ?.also { metadataService.deleteAll(it) }

        throw AuthorizationException(
            "Bruker har ikke tilgang",
            errorType = SoknadApiErrorType.NoAccess,
        )
    }

    companion object {
        private val logger by logger()
        private val FILTERED_URIS =
            listOf(
                "/vedlegg/konverter",
            )
    }
}
