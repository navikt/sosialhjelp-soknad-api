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
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Slf4j
public class OpenAmLoginFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(OpenAmLoginFilter.class);

    private final OpenAMUserInfoService userInfoService;

    public static final String NAV_ESSO_COOKIE_NAVN = "nav-esso";

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

        Subject subject = authenticate(httpServletRequest, httpServletResponse);
        if (subject == null) {
            unAuthenticated(httpServletRequest, httpServletResponse);
        } else {
            log.info("DEBUG - LoginFilter subject:  " + subject);
            SubjectHandler.withSubject(subject, () -> chain.doFilter(request, response));
        }
    }

    private void unAuthenticated(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        Optional<String> optionalRedirectUrl = Optional.empty();
        if ("application/json".equals(httpServletRequest.getHeader("Accept")) || optionalRedirectUrl.isEmpty()) {
            httpServletResponse.sendError(SC_UNAUTHORIZED);
        } else {
            httpServletResponse.sendRedirect(optionalRedirectUrl.get());
        }
    }

    public Subject authenticate(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String requestEksternSsoToken = getRequestEksternSsoToken(httpServletRequest);
        if (StringUtils.nullOrEmpty(requestEksternSsoToken)) {
            log.info("Ingen Ekstern sso-token (SAML) i request mot " + httpServletRequest.getRequestURI());
            return null;
        }

        Optional<Subject> userInfo = userInfoService.convertTokenToSubject(requestEksternSsoToken);
        if (userInfo.isEmpty()) {
            log.info("DEBUG - OpenAMLoginFilter authenticate userInfo is empty");
            removeSsoToken(httpServletRequest, httpServletResponse);
            return null;
        }

        log.info("DEBUG - OpenAMLoginFilter authenticate userInfo is present" + userInfo.get());
        return userInfo.get();
    }

    private String getRequestEksternSsoToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
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