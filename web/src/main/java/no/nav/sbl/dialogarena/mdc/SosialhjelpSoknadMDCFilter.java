package no.nav.sbl.dialogarena.mdc;

import no.nav.modig.core.context.SubjectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static no.nav.sbl.dialogarena.mdc.MDCOperations.generateCallId;
import static no.nav.sbl.dialogarena.mdc.MDCOperations.putToMDC;
import static no.nav.sbl.dialogarena.mdc.MDCOperations.remove;

public class SosialhjelpSoknadMDCFilter extends OncePerRequestFilter {

    protected static final Logger log = LoggerFactory.getLogger(SosialhjelpSoknadMDCFilter.class.getName());

    private static final String CALL_ID = "callId";
    private static final String USER_ID = "userId";
    private static final String CONSUMER_ID = "consumerId";

    private SubjectHandler subjectHandler;

    public SosialhjelpSoknadMDCFilter() {
    }

    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        this.subjectHandler = SubjectHandler.getSubjectHandler();
    }

    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String userId = this.subjectHandler.getUid() != null ? this.subjectHandler.getUid() : "";
        String consumerId = this.subjectHandler.getConsumerId() != null ? this.subjectHandler.getConsumerId() : "";
        String callId = generateCallId();
        putToMDC(CALL_ID, callId);
        putToMDC(USER_ID, userId);
        putToMDC(CONSUMER_ID, consumerId);

        try {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } finally {
            remove("callId");
            remove("userId");
            remove("consumerId");
        }

    }
}
