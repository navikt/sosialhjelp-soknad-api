package no.nav.sbl.dialogarena.oidc;

import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.security.oidc.jaxrs.servlet.JaxrsOIDCTokenValidationFilter;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.inject.Inject;
import javax.servlet.*;
import java.io.IOException;

import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.isRunningWithOidc;

public class OidcTokenValidatorFilter implements Filter {

    @Inject
    private JaxrsOIDCTokenValidationFilter jaxrsOIDCTokenValidationFilter;

    @Override
    public void init(FilterConfig filterConfig){
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!isRunningWithOidc() || MockUtils.isTillatMockRessurs()) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            jaxrsOIDCTokenValidationFilter.doFilter(servletRequest, servletResponse, filterChain);
        }
    }

    @Override
    public void destroy() {
        jaxrsOIDCTokenValidationFilter.destroy();
    }
}
