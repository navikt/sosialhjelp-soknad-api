package no.nav.sbl.dialogarena.oidc;

import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration;
import no.nav.security.token.support.jaxrs.servlet.JaxrsJwtTokenValidationFilter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class FakeOidcTokenValidatorFilter extends JaxrsJwtTokenValidationFilter {

    public FakeOidcTokenValidatorFilter(MultiIssuerConfiguration oidcConfig) {
        super(oidcConfig);
    }

    @Override
    public void init(FilterConfig filterConfig){
        // do nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // do nothing
        filterChain.doFilter(servletRequest, servletResponse);
    }
}

