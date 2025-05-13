package no.nav.sosialhjelp.soknad.v2.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.HandlerMapping
import java.util.UUID
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as personId

@Component
class SoknadAccessInterceptor(
    private val soknadService: SoknadService,
    private val personService: PersonService,
    private val metadataService: SoknadMetadataService,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (hasAdressebeskyttelse(request.requestURI)) handleHasAdressebeskyttelse()
        if (request.method == "OPTIONS") return true

        getSoknadId(request)?.also { checkPersonId(UUID.fromString(it), request.method) }

        return true
    }

    // hente ikke fra potensiell cache ved sending av soknad
    private fun hasAdressebeskyttelse(uri: String): Boolean =
        when (uri.contains("/send")) {
            true -> personService.onSendSoknadHasAdressebeskyttelse(personId())
            false -> personService.hasAdressebeskyttelse(personId())
        }

    private fun handleHasAdressebeskyttelse() {
        soknadService.findOpenSoknadIds(personId())
            .takeIf { it.isNotEmpty() }
            ?.also { metadataService.deleteAll(it) }

        throw AuthorizationException("Bruker har ikke tilgang")
    }

    private fun checkPersonId(
        soknadId: UUID,
        method: String,
    ) {
        val eier = soknadService.findOrError(soknadId).eierPersonId

        if (method in listOf("GET", "POST", "PUT", "DELETE") && eier != personId()) {
            throw AuthorizationException("Bruker har ikke tilgang til søknaden")
        }
    }
}

private fun getSoknadId(request: HttpServletRequest): String? {
    val pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<*, *>
    return pathVariables?.get("soknadId") as String?
}
