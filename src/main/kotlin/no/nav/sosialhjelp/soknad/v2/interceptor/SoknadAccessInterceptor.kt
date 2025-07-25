package no.nav.sosialhjelp.soknad.v2.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.config.SoknadApiHandlerInterceptor
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerMapping
import java.util.UUID
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as personId

@Component
class SoknadAccessInterceptor(
    private val soknadService: SoknadService,
) : SoknadApiHandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (request.method == "OPTIONS") return true

        getSoknadId(request)?.also { checkPersonId(it.convertIdToUUID(), request.method) }

        return true
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

private fun String.convertIdToUUID(): UUID =
    runCatching { UUID.fromString(this) }
        .getOrElse { e ->
            if (e is IllegalArgumentException && e.message?.contains("Invalid UUID string") == true) {
                throw AuthorizationException("Ugyldig format på SoknadId: $this")
            }
            throw e
        }

private fun getSoknadId(request: HttpServletRequest): String? {
    val pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<*, *>
    return pathVariables?.get("soknadId") as String?
}
