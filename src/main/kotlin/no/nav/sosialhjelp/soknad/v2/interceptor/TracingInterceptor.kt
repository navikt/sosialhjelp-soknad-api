package no.nav.sosialhjelp.soknad.v2.interceptor

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.config.SoknadApiHandlerInterceptor
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerMapping

@Component
@Profile("!local")
class TracingInterceptor : SoknadApiHandlerInterceptor {
    private val log by logger()

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val soknadId = request.getSoknadId()

        if (soknadId != null) {
            val currentSpan = Span.current()
            if (!currentSpan.spanContext.isValid) {
                log.warn("Invalid span context")
            }
            currentSpan.setAttribute("soknadId", soknadId)
        }
        return true
    }
}

private fun HttpServletRequest.getSoknadId(): String? {
    val pathVariables = getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<*, *>
    return pathVariables?.get("soknadId") as String?
}
