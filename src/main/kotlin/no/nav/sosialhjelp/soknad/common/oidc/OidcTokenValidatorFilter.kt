package no.nav.sosialhjelp.soknad.common.oidc

import no.nav.security.token.support.jaxrs.servlet.JaxrsJwtTokenValidationFilter
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

class OidcTokenValidatorFilter(
    private val jaxrsJwtTokenValidationFilter: JaxrsJwtTokenValidationFilter
) : Filter {

    override fun init(filterConfig: FilterConfig) {
        //no op
    }

    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        jaxrsJwtTokenValidationFilter!!.doFilter(servletRequest, servletResponse, filterChain)
    }

    override fun destroy() {
        jaxrsJwtTokenValidationFilter!!.destroy()
    }
}
