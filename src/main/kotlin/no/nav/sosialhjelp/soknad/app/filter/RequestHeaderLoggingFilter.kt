package no.nav.sosialhjelp.soknad.app.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestHeaderLoggingFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        log.info("--- Incoming Request Headers ---")
        val headerNames = request.getHeaderNames()
        while (headerNames.hasMoreElements()) {
            val headerName = headerNames.nextElement()
            log.info("{}: {}", headerName, request.getHeader(headerName))
        }
        log.info("--------------------------------")
        filterChain.doFilter(request, response)
    }

    companion object {
        val log by logger()
    }
}
