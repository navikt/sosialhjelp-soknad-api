package no.nav.sbl.dialogarena.saml;

import no.nav.common.auth.Subject;
import no.nav.common.auth.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SamlUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

public class OpenAmLoginFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(OpenAmLoginFilter.class);

    private final OpenAMUserInfoService userInfoService;

    public static final String NAV_ESSO_COOKIE_NAVN = "nav-esso";
    public static final List<String> UNPROTECDED_BASE_PATHS = List.of(
            "/sosialhjelp/soknad-api/metadata/ping",
            "/sosialhjelp/soknad-api/metadata/oidc/"
    );

    public OpenAmLoginFilter() {
        this.userInfoService = new OpenAMUserInfoService();
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        Subject subject = null;

        if (isPathProtectedBySAML(httpServletRequest.getRequestURI())) {
            try {
                subject = authenticate(httpServletRequest);
            } catch (SamlUnauthorizedException e) {
                log.warn(e.getMessage());
                removeSsoToken(httpServletRequest, httpServletResponse);
                httpServletResponse.sendError(SC_UNAUTHORIZED);
            }
        }
        SubjectHandler.withSubject(subject, () -> chain.doFilter(request, response));
    }

    static boolean isPathProtectedBySAML(String requestPath) {
        return UNPROTECDED_BASE_PATHS.stream().noneMatch(requestPath::startsWith);
    }

    public Subject authenticate(HttpServletRequest httpServletRequest) {
        String requestEksternSsoToken = getRequestEksternSsoToken(httpServletRequest.getCookies());
        return userInfoService.convertTokenToSubject(requestEksternSsoToken);
    }

    private String getRequestEksternSsoToken(Cookie[] cookies) throws SamlUnauthorizedException {
        if (cookies == null) {
            throw new SamlUnauthorizedException("Ingen SAML-token funnet i request, da requesten ikke inneholder noen cookies. ");
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(NAV_ESSO_COOKIE_NAVN) && cookie.getValue() != null) {
                return cookie.getValue();
            }
        }
        throw new SamlUnauthorizedException(String.format("Ingen SAML-token funnet i request, da requesten ikke inneholder cookie med navn:  %s", NAV_ESSO_COOKIE_NAVN));
    }

    private void removeSsoToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(NAV_ESSO_COOKIE_NAVN)) {
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    cookie.setDomain(".nav.no");
                    response.addCookie(cookie);
                }
            }
        }
    }

    @Override
    public void destroy() {

    }
}