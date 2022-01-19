package no.nav.sosialhjelp.soknad.common.oidc

import no.nav.security.token.support.jaxrs.servlet.JaxrsJwtTokenValidationFilter
import org.springframework.web.context.support.SpringBeanAutowiringSupport
import java.io.IOException
import javax.inject.Inject
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

class OidcTokenValidatorFilter : Filter {

    @Inject
    private val jaxrsJwtTokenValidationFilter: JaxrsJwtTokenValidationFilter? = null

    override fun init(filterConfig: FilterConfig) {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this)
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        jaxrsJwtTokenValidationFilter!!.doFilter(servletRequest, servletResponse, filterChain)
    }

    override fun destroy() {
        jaxrsJwtTokenValidationFilter!!.destroy()
    }
}
