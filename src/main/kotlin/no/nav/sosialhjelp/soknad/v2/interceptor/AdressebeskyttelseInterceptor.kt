package no.nav.sosialhjelp.soknad.v2.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
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
        if (request.isFilteredUri()) return true

        // finnes det ingen auth header, får vi heller ikke sjekket adressebeskyttelse
        authorization()?.also { if (hasAdressebeskyttelse(request.isSendPath())) handleHasAdressebeskyttelse() }
        return true
    }

    private fun authorization() = runCatching { SubjectHandlerUtils.getTokenOrNull() }.getOrNull()

    private fun HttpServletRequest.isSendPath() = requestURI.matchesRegex(BASE_PATH + SEND_PATH)

    // henter ikke fra cache ved sending for å sjekke at person ikke har fått adressebeskyttelse underveis i søknaden
    private fun hasAdressebeskyttelse(isSendPath: Boolean): Boolean {
        return when (isSendPath) {
            true -> personService.onSendSoknadHasAdressebeskyttelse(getUserIdFromToken())
            false -> personService.hasAdressebeskyttelse(getUserIdFromToken())
        }
    }

    private fun handleHasAdressebeskyttelse() {
        soknadService.findOpenSoknadIds(getUserIdFromToken())
            .takeIf { it.isNotEmpty() }
            ?.also { metadataService.deleteAll(it) }

        throw AuthorizationException("Ikke tilgang til søknad", type = SoknadApiErrorType.NoAccess)
    }

    private fun HttpServletRequest.isFilteredUri() = FILTERED_URIS.any { requestURI.matchesRegex(BASE_PATH + it) }

    private fun String.matchesRegex(pattern: String) = Regex("^$pattern\$").matches(this)

    companion object {
        private const val BASE_PATH = "/sosialhjelp/soknad-api"
        private const val REGEX_UUID = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[4][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"
        private const val SEND_PATH = "/soknad/$REGEX_UUID/send"

        private val FILTERED_URIS =
            listOf(
                "/dittnav/pabegynte/aktive",
                "/minesaker/innsendte",
                "/internal/isAlive",
                "/internal/prometheus",
                "/feature-toggle",
            )
    }
}
