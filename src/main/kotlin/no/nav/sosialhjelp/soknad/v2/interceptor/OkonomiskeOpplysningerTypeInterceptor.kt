package no.nav.sosialhjelp.soknad.v2.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.config.SoknadApiHandlerInterceptor
import org.springframework.stereotype.Component

// Det kan være vanskelig å grave i feil som oppstår ved put-kall til /okonomiskeOpplysninger
@Component
class OkonomiskeOpplysningerTypeInterceptor : SoknadApiHandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (request.requestURI.contains("/okonomiskeOpplysninger") && request.method == "PUT") {
            logger.info("Oppdatering av opplysning: ${request.getParameter("type")}")
        }
        return true
    }

    companion object {
        private val logger by logger()
    }
}
