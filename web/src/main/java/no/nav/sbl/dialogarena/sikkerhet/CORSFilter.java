package no.nav.sbl.dialogarena.sikkerhet;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;

public class CORSFilter implements Filter {

    private static final List<String> ALLOWED_ORIGINS = asList(
            "http://localhost:3000",
            "http://localhost:8080",
            "https://soknadsosialhjelp-t1.nais.oera-q.local",
            "https://soknadsosialhjelp-q0.nais.oera-q.local"
            );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String origin = "*";
        if (servletRequest instanceof  HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
            origin = httpRequest.getHeader("Origin");
        }

        if (ALLOWED_ORIGINS.contains(origin)) {
            HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
            httpResponse.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, X-XSRF-TOKEN");
            httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            filterChain.doFilter(servletRequest, httpResponse);
        }
    }

    @Override
    public void destroy() {}
}
