package no.nav.sosialhjelp.soknad.tracing

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.HandlerMapping

@Component
class TracingInterceptor : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val behandlingsId = getBehandlingsId(request)

        if (behandlingsId != null) {
            Span.current().setAttribute("behandlingsid", behandlingsId)
        }

        return true
    }

    private fun getBehandlingsId(request: HttpServletRequest): String? {
        val pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<*, *>
        return pathVariables?.get("behandlingsId") as String?
    }
}
