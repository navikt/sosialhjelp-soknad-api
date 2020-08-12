package no.nav.sbl.dialogarena.saml;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.auth.Subject;
import no.nav.common.auth.SubjectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Slf4j
public class LoginFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(LoginFilter.class);

    private final LoginProvider loginProvider;

    public LoginFilter() {
        this.loginProvider = new OpenAMLoginFilter();
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        Subject subject = loginProvider.authenticate(httpServletRequest, httpServletResponse);
        if (subject == null) {
            unAuthenticated(httpServletRequest, httpServletResponse);
        } else {
            log.info("DEBUG - LoginFilter subject:  " + subject);
            SubjectHandler.withSubject(subject, () -> chain.doFilter(request, response));
        }
    }

    private void unAuthenticated(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        Optional<String> optionalRedirectUrl = loginProvider.redirectUrl(httpServletRequest, httpServletResponse);
        if ("application/json".equals(httpServletRequest.getHeader("Accept")) || optionalRedirectUrl.isEmpty()) {
            httpServletResponse.sendError(SC_UNAUTHORIZED);
        } else {
            httpServletResponse.sendRedirect(optionalRedirectUrl.get());
        }
    }

    @Override
    public void destroy() {

    }
}