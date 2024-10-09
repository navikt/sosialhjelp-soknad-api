package no.nav.sosialhjelp.soknad.tracing

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.HandlerMapping

@Component
class TracingInterceptor : HandlerInterceptor {
    private val log by logger()

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val behandlingsId = getBehandlingsId(request)

        if (behandlingsId != null) {
            val currentSpan = Span.current()
            if (!currentSpan.spanContext.isValid) {
                log.warn("Invalid span context")
            }
            currentSpan.setAttribute("behandlingsid", behandlingsId)
        }

        return true
    }

    private fun getBehandlingsId(request: HttpServletRequest): String? {
        val pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<*, *>
        return pathVariables?.get("behandlingsId") as String?
    }
}
