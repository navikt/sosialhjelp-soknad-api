package no.nav.sosialhjelp.soknad.app.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.sync.Mutex
import no.nav.sosialhjelp.soknad.app.service.RequestDelayService
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.HandlerMapping

@Component
/**
 * En interceptor for å forsinke forespørsler for å unngå redigeringskonflikter.
 *
 * Denne interceptorklassen forsinker en request opptil ett sekund dersom det er
 * potensiell fare for en redigeringskonflikt. Ved hjelp av `RequestDelayService` blir
 * låsen hentet basert på behandlingsId, og hvis låsen blir ervervet, blir den føyd til
 * request-atributter for frigjøring etter fullføring.
 *
 */
class ConflictAvoidanceDelayInterceptor(
    private val requestDelayService: RequestDelayService
) : HandlerInterceptor {
    /**
     * Pauser en request i opptil 200ms for å forhindre en redigeringskonflikt.
     */
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val behandlingsId = getBehandlingsId(request)

        // Om URLen ikke inneholder behandlingsId, eller kun er for lesing, returnerer vi umiddelbart.
        if (behandlingsId != null && !isSafe(request))
            requestDelayService.getLock(behandlingsId)?.let { request.setAttribute("acquiredLock", it) }

        return true
    }

    private fun getBehandlingsId(request: HttpServletRequest): String? {
        val pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<*, *>
        return pathVariables?.get("behandlingsId") as String?
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        (request.getAttribute("acquiredLock") as? Mutex)?.let { requestDelayService.releaseLock(it) }
    }

    private fun isSafe(request: HttpServletRequest): Boolean = request.method in setOf("GET", "HEAD", "OPTIONS")
}
