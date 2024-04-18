package no.nav.sosialhjelp.soknad.app.filter

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import org.springframework.stereotype.Component

@Component
class CORSFilter : Filter {
    override fun init(filterConfig: FilterConfig?) {
        super.init(filterConfig)
    }

    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain,
    ) {
        val httpResponse = response as HttpServletResponse
        val origin = if (request is HttpServletRequest) (request.getHeader("Origin")) else null

        if (MiljoUtils.isNonProduction() || ALLOWED_ORIGINS.contains(origin)) {
            httpResponse.setHeader("Access-Control-Allow-Origin", origin)
            httpResponse.setHeader(
                "Access-Control-Allow-Headers",
                "Origin, Content-Type, Accept, X-XSRF-TOKEN, Nav-Call-Id, Authorization, sentry-trace, baggage",
            )
            httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true")
        }
        chain.doFilter(request, httpResponse)
    }

    companion object {
        private val ALLOWED_ORIGINS =
            listOf(
                "https://tjenester.nav.no",
                "https://www.nav.no",
            )
    }
}
