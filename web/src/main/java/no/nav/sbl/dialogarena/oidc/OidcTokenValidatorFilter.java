package no.nav.sbl.dialogarena.oidc;

import no.nav.security.oidc.jaxrs.servlet.JaxrsOIDCTokenValidationFilter;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import no.nav.sbl.dialogarena.mock.TjenesteMockRessurs;

import javax.inject.Inject;
import javax.servlet.*;
import java.io.IOException;

public class OidcTokenValidatorFilter implements Filter {

    @Inject
    private JaxrsOIDCTokenValidationFilter jaxrsOIDCTokenValidationFilter;

    @Override
    public void init(FilterConfig filterConfig){
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (TjenesteMockRessurs.isTillatMockRessurs()) {
            filterChain.doFilter(servletRequest, servletResponse);
        }

        jaxrsOIDCTokenValidationFilter.doFilter(servletRequest, servletResponse, filterChain);
    }

    @Override
    public void destroy() {
        jaxrsOIDCTokenValidationFilter.destroy();
    }
}
