package no.nav.sbl.dialogarena.server;

import no.nav.brukerdialog.security.domain.IdentType;
import no.nav.common.auth.SsoToken;
import no.nav.common.auth.Subject;
import no.nav.common.auth.SubjectHandler;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Collections;

public class SosialhjelpAuthenticationFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        SsoToken token = SsoToken.oidcToken("hansolo", Collections.emptyMap());
        Subject subject = new Subject("123", IdentType.EksternBruker, token);
        SubjectHandler.withSubject(subject, () -> {
            filterChain.doFilter(servletRequest, servletResponse);
        });
    }

    @Override
    public void destroy() {

    }
}
