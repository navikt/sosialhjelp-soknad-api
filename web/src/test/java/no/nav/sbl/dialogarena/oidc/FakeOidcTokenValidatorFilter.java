package no.nav.sbl.dialogarena.oidc;

import no.nav.security.oidc.configuration.MultiIssuerConfiguration;
import no.nav.security.oidc.jaxrs.servlet.JaxrsOIDCTokenValidationFilter;

import javax.servlet.*;
import java.io.IOException;

public class FakeOidcTokenValidatorFilter extends JaxrsOIDCTokenValidationFilter {

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

