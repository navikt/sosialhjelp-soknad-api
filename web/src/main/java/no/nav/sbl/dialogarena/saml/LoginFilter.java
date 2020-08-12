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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Slf4j
public class LoginFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(LoginFilter.class);

    public static final List<String> DEFAULT_PUBLIC_PATHS = Arrays.asList(
            "/internal/.*",
            "/ws/.*",
            "/api/ping"
    );

    private final LoginProvider loginProvider;
    private final List<String> publicPaths;
    private List<Pattern> publicPatterns;

    public LoginFilter() {
        this.loginProvider = new OpenAMLoginFilter();
        this.publicPaths = DEFAULT_PUBLIC_PATHS;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String contextPath = contextPath(filterConfig);
        this.publicPatterns = publicPaths.stream()
                .map(path -> "^" + contextPath + path)
                .map(Pattern::compile)
                .collect(toList());
        log.info("initialized {} with public patterns: {}", LoginFilter.class.getName(), publicPatterns);
    }



    private String contextPath(FilterConfig filterConfig) {
        String contextPath = filterConfig.getServletContext().getContextPath();
        if (contextPath == null || contextPath.length() <= 1) {
            contextPath = "";
        }
        return contextPath;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        Optional<Subject> optionalSubject = resolveSubject(httpServletRequest, httpServletResponse);
        if (optionalSubject.isPresent()) {
            log.info("DEBUG - LoginFilter subject:  " + optionalSubject.get());
            SubjectHandler.withSubject(optionalSubject.get(), () -> chain.doFilter(request, response));
        } else if (isPublic(httpServletRequest)) {
            chain.doFilter(request, response);
        } else {
            unAuthenticated(httpServletRequest, httpServletResponse);
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

    boolean isPublic(HttpServletRequest httpServletRequest) {
        return publicPatterns.stream().anyMatch(p -> p.matcher(httpServletRequest.getRequestURI()).matches());
    }

    private Optional<Subject> resolveSubject(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return loginProvider.authenticate(httpServletRequest, httpServletResponse);
    }

    @Override
    public void destroy() {

    }
}