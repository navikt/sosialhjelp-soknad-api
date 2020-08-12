package no.nav.sbl.dialogarena.saml;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.auth.Subject;
import no.nav.common.auth.SubjectHandler;
import no.nav.sbl.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Slf4j
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

        if (isPathProtectedBySAML(httpServletRequest.getRequestURI())) {
            Subject subject = authenticate(httpServletRequest, httpServletResponse);
            if (subject == null) {
                httpServletResponse.sendError(SC_UNAUTHORIZED);
            } else {
                log.info("DEBUG - LoginFilter subject:  " + subject);
//                SubjectHandler.withSubject(subject, () -> chain.doFilter(request, response));

                //Kun en test av saksoversikt. Denne skal ikke være her:
                httpServletResponse.sendError(SC_UNAUTHORIZED);
            }
        }
        chain.doFilter(request, response);
    }

    static boolean isPathProtectedBySAML(String requestPath) {
        return UNPROTECDED_BASE_PATHS.stream().noneMatch(requestPath::startsWith);
    }

    public Subject authenticate(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String requestEksternSsoToken = getRequestEksternSsoToken(httpServletRequest.getCookies());
        if (StringUtils.nullOrEmpty(requestEksternSsoToken)) {
            log.info("Ingen Ekstern sso-token (SAML) i request");
            return null;
        }

        Subject subject = userInfoService.convertTokenToSubject(requestEksternSsoToken);
        if (subject == null) {
            log.info("DEBUG - OpenAMLoginFilter authenticate userInfo is empty");
            removeSsoToken(httpServletRequest, httpServletResponse);
            return null;
        }

        log.info("DEBUG - OpenAMLoginFilter authenticate userInfo is present " + subject);
        return subject;
    }

    private String getRequestEksternSsoToken(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(NAV_ESSO_COOKIE_NAVN)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
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