package no.nav.sosialhjelp.soknad.app.filter

import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class HeaderFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        response.setHeader("X-Content-Type-Options", "nosniff")
        response.setHeader("X-XSS-Protection", "1; mode=block")
        response.setHeader("Cache-Control", "private, max-age=0, no-cache, no-store")
        filterChain.doFilter(request, response)
    }
}
