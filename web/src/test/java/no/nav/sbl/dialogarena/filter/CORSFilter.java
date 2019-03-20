package no.nav.sbl.dialogarena.filter;

import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CORSFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String origin = "*";
        if (servletRequest instanceof  HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
            origin = httpRequest.getHeader("Origin");
        }

        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        httpResponse.setHeader("Access-Control-Allow-Origin", origin);
        httpResponse.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, X-XSRF-TOKEN");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        filterChain.doFilter(servletRequest, httpResponse);
    }

    @Override
    public void destroy() {}
}
