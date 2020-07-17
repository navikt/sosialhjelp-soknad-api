package no.nav.sbl.dialogarena.oidc;

import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.security.token.support.jaxrs.servlet.JaxrsJwtTokenValidationFilter;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.isRunningWithOidc;

public class OidcTokenValidatorFilter implements Filter {

    @Inject
    private JaxrsJwtTokenValidationFilter jaxrsJwtTokenValidationFilter;

    @Override
    public void init(FilterConfig filterConfig){
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!isRunningWithOidc() || MockUtils.isTillatMockRessurs()) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            jaxrsJwtTokenValidationFilter.doFilter(servletRequest, servletResponse, filterChain);
        }
    }

    @Override
    public void destroy() {
        jaxrsJwtTokenValidationFilter.destroy();
    }
}
