package no.nav.sbl.dialogarena.server;

import no.nav.brukerdialog.security.domain.IdentType;
import no.nav.common.auth.SsoToken;
import no.nav.common.auth.Subject;
import no.nav.common.auth.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandlerWrapper;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class SosialhjelpAuthenticationFilter extends HttpFilter {

    @Inject
    private SubjectHandlerWrapper subjectHandlerWrapper;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        /*if (MockUtils.isTillatMockRessurs()) {
            String mockRessursUid = (String)servletRequest.getSession().getAttribute("mockRessursUid");
            if (mockRessursUid != null) {
                SsoToken ssoToken = SsoToken.oidcToken("token", Collections.emptyMap());
                Subject subject = new Subject(mockRessursUid, IdentType.EksternBruker, ssoToken);
                SubjectHandler.withSubject(subject, () -> {
                    filterChain.doFilter(servletRequest, servletResponse);
                });
            }
            else {
                filterChain.doFilter(servletRequest, servletResponse);
            }
        }
        else {
            Subject subject = SubjectHandler.getSubject().orElseThrow(() -> new AuthorizationException("Missing userId"));
            SubjectHandler.withSubject(subject, () -> {
                filterChain.doFilter(servletRequest, servletResponse);
            });
        }*/
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
