package no.nav.sbl.dialogarena.mdc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static no.nav.sbl.dialogarena.mdc.MDCOperations.generateCallId;
import static no.nav.sbl.dialogarena.mdc.MDCOperations.putToMDC;
import static no.nav.sbl.dialogarena.mdc.MDCOperations.remove;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.HeaderConstants.HEADER_CALL_ID;

public class SosialhjelpSoknadMDCFilter extends OncePerRequestFilter {

    protected static final Logger log = LoggerFactory.getLogger(SosialhjelpSoknadMDCFilter.class.getName());

    private static final String CALL_ID = "callId";
    private static final String CONSUMER_ID = "consumerId";

    public SosialhjelpSoknadMDCFilter() {
    }

    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
    }

    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String callId = Optional.ofNullable(httpServletRequest.getHeader(HEADER_CALL_ID))
                .orElse(generateCallId());
        putToMDC(CALL_ID, callId);
        putToMDC(CONSUMER_ID, getConsumerId());

        try {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } finally {
            remove(CALL_ID);
            remove(CONSUMER_ID);
        }
    }
}
