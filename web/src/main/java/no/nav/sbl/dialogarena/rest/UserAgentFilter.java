package no.nav.sbl.dialogarena.rest;

import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import ua_parser.Client;
import ua_parser.Parser;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UserAgentFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(UserAgentFilter.class);

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String uaHeader = request.getHeader("User-Agent");
        try {
            String requestPath = request.getRequestURI();
            logger.info("RequestURI: {}", requestPath);

            Parser parser = new Parser();
            Client client = parser.parse(uaHeader);
            String family = client.userAgent.family;
            String majorVersion = client.userAgent.major;
            String os = client.os.family;
            logger.info("family: {}, majorVersion: {}, os: {}", family, majorVersion, os);

            Event browserEvent = MetricsFactory.createEvent("user-agent");
            browserEvent.addFieldToReport("os", os);
            browserEvent.addFieldToReport("browser", family);
            browserEvent.addFieldToReport("majorVersion", majorVersion);
            browserEvent.report();
        } catch (Exception e) {
            logger.info("Unable to parse User-Agent: {}", uaHeader);
        }

        filterChain.doFilter(request, response);
    }

}
