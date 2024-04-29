package no.nav.sosialhjelp.soknad.v2.config.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.tilgangskontroll.XsrfGenerator
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.HandlerMapping
import java.util.UUID

@Component
class SoknadAccessInterceptor(private val soknadService: SoknadService) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val method = request.method
        val soknadId = getSoknadId(request) ?: return true
        val soknad = soknadService.getSoknad(UUID.fromString(soknadId))
        val userId = SubjectHandlerUtils.getUserIdFromToken()
        if (method == "GET") {
            if (soknad.eierPersonId != userId) {
                throw AuthorizationException("Bruker har ikke tilgang til søknaden")
            }
        } else if (method == "POST" || method == "PUT" || method == "DELETE") {
            val xsrfToken = request.getHeader("X-XSRF-TOKEN")
            XsrfGenerator.sjekkXsrfToken(xsrfToken, soknadId, false)
            if (soknad.eierPersonId != userId) {
                throw AuthorizationException("Bruker har ikke tilgang til søknaden")
            }
        }
        return true
    }

    private fun getSoknadId(request: HttpServletRequest): String? {
        val pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<*, *>
        return pathVariables?.get("soknadId") as String?
    }
}
