package no.nav.sosialhjelp.soknad.app.soknadlock

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.HandlerMapping
import kotlin.reflect.cast

/**
 * En interceptor som forsinker skriveforespørsler for å unngå redigeringskonflikter.
 *
 * Denne interceptorklassen forsinker en request opptil ett sekund dersom det er
 * potensiell fare for en redigeringskonflikt. Ved hjelp av `RequestDelayService` blir
 * låsen hentet basert på behandlingsId, og hvis låsen blir ervervet, blir den føyd til
 * request-atributter for frigjøring etter fullføring.
 *
 */
@Component
@Profile("!no-interceptor")
class ConflictAvoidanceDelayInterceptor(
    private val soknadLockManager: SoknadLockManager,
) : HandlerInterceptor {
    companion object {
        val LOCK_ATTRIBUTE_NAME = ConflictAvoidanceDelayInterceptor::class.java.canonicalName + ".LOCK"
        private val log = LoggerFactory.getLogger(ConflictAvoidanceDelayInterceptor::class.java)
    }

    /**
     * Blokkerer writes mot søknad i inntil SoknadLockManager.LOCK_TIMEOUT_MS for å forebygge versjonskonflikter.
     * @see SoknadLockManager.LOCK_TIMEOUT_MS
     */
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        // dersom soknadLockManager er disabled, er hele interceptoren inaktiv.
        if (!soknadLockManager.enabled) return true

        val behandlingsId = getBehandlingsId(request)

        // Om URLen ikke inneholder behandlingsId returnerer vi umiddelbart.
        if (behandlingsId == null) return true

        soknadLockManager.getLock(behandlingsId)?.let { request.setAttribute(LOCK_ATTRIBUTE_NAME, it) }

        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        getRequestLock(request)?.let {
            try {
                soknadLockManager.releaseLock(it)
            } catch (e: Exception) {
                log.warn("Failed to release lock for ${getBehandlingsId(request)}", e)
            }
        }
    }

    private fun getBehandlingsId(request: HttpServletRequest): String? {
        val pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<*, *>
        return pathVariables?.get("behandlingsId") as String?
    }

    private fun getRequestLock(request: HttpServletRequest): SoknadLockManager.TimestampedLock? {
        val expectedType = SoknadLockManager.TimestampedLock::class
        val lockAttribute = request.getAttribute(LOCK_ATTRIBUTE_NAME)

        return when (expectedType.isInstance(lockAttribute)) {
            true -> expectedType.cast(lockAttribute)
            else -> null
        }
    }
}
