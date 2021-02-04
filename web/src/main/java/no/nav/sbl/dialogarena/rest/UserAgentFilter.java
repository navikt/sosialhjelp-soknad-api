package no.nav.sbl.dialogarena.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UserAgentFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(UserAgentFilter.class);

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        // TODO: Enable telling av user-agent når trykket på sensu / influxdb er lavere igjen / finne bedre måte å telle user-agent på.
        /*
        if (countUserAgentForRequest(request)) {
            String uaHeader = request.getHeader("User-Agent");
            try {
                Parser parser = new Parser();
                Client client = parser.parse(uaHeader);

                MetricsFactory.createEvent("soknad.user-agent")
                        .addTagToReport("browser-family", client.userAgent.family)
                        .addTagToReport("browser-major-version", client.userAgent.major)
                        .addTagToReport("os-family", client.os.family)
                        .addTagToReport("os-major-versjon", client.os.major)
                        .report();
                if (client.userAgent.family.equals("Other")) {
                    logger.info("Request URI for browser: Other, {}", request.getRequestURI());
                }
            } catch (Exception e) {
                logger.info("Unable to parse User-Agent: {}", uaHeader);
            }
        }
         */
        filterChain.doFilter(request, response);
    }

    private boolean countUserAgentForRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/internal/isAlive")) {
            return false;
        }
        if (requestURI.contains("/metadata")) {
            return false;
        }
        return true;
    }

}
