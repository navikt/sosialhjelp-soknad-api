package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;

public class CORSFilter implements Filter {
    private static final List<String> ALLOWED_ORIGINS = asList(
            "https://tjenester.nav.no",
            "https://www.nav.no");

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        String origin = "*";
        if (servletRequest instanceof  HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
            origin = httpRequest.getHeader("Origin");
        }

        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        if (!ServiceUtils.isRunningInProd() || ALLOWED_ORIGINS.contains(origin)) {
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
            httpResponse.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, X-XSRF-TOKEN");
            httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        }
        filterChain.doFilter(servletRequest, httpResponse);
    }

    @Override
    public void destroy() {}
}
