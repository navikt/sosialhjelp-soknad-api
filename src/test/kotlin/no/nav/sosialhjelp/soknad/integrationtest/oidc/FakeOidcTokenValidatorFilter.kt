package no.nav.sosialhjelp.soknad.integrationtest.oidc

import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import no.nav.security.token.support.jaxrs.servlet.JaxrsJwtTokenValidationFilter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

class FakeOidcTokenValidatorFilter(
    oidcConfig: MultiIssuerConfiguration?
) : JaxrsJwtTokenValidationFilter(oidcConfig) {

    override fun init(filterConfig: FilterConfig) {
        // do nothing
    }

    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        // do nothing
        filterChain.doFilter(servletRequest, servletResponse)
    }
}
