package no.nav.sbl.dialogarena.sikkerhet;

import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;

import javax.servlet.*;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class MultipartLogFilter implements Filter {
    public static final Logger log = getLogger(MultipartLogFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        if (servletRequest.getContentType() != null && servletRequest.getContentType().toLowerCase().contains("multipart/form-data")) {
            String path = (servletRequest instanceof Request) ? ((Request) servletRequest).getRequestURI() : "kunne ikke hente";
            if (servletRequest.getContentLength() == 0) {
                log.error("Request har content-type: multipart/form-data med contentLength 0. Det virker som bruker forsøker å bruke opplastningsfunksjonen uten filer. Path: {}", path);
            } else {
                log.debug("Request har content-type: multipart/form-data med contentLength {}. Path: {}", servletRequest.getContentLength(), path);
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {}
}
