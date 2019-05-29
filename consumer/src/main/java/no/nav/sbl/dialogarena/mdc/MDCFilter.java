package no.nav.sbl.dialogarena.mdc;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlSubjectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Se <a href=http://confluence.adeo.no/display/Modernisering/MDCFilter>Utviklerhåndbok - Logging - Sporingslogging -
 * MDCFilter</a> for informasjon om filteret og hvordan det skal brukes.
 */
public class MDCFilter extends OncePerRequestFilter {
    protected static final Logger log = LoggerFactory.getLogger(MDCFilter.class.getName());

    private SamlSubjectHandler subjectHandler;

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        subjectHandler = SamlSubjectHandler.getSubjectHandler();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
            throws ServletException, IOException {
        log.debug("Entering filter to extract values and put on MDC for logging");

        String userId = subjectHandler.getUid() != null ? subjectHandler.getUid() : "";
        String consumerId = subjectHandler.getConsumerId() != null ? subjectHandler.getConsumerId() : "";
        String callId = MDCOperations.generateCallId();

        MDCOperations.putToMDC(MDCOperations.MDC_CALL_ID, callId);
        MDCOperations.putToMDC(MDCOperations.MDC_USER_ID, userId);
        MDCOperations.putToMDC(MDCOperations.MDC_CONSUMER_ID, consumerId);
        log.debug("Values added");

        try {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } finally {
            MDCOperations.remove(MDCOperations.MDC_CALL_ID);
            MDCOperations.remove(MDCOperations.MDC_USER_ID);
            MDCOperations.remove(MDCOperations.MDC_CONSUMER_ID);
            log.debug("Cleared MDC session");
        }
    }
}
