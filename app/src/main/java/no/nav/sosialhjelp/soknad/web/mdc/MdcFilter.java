package no.nav.sosialhjelp.soknad.web.mdc;

import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.util.Optional;

import static no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations.MDC_BEHANDLINGS_ID;
import static no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations.MDC_CALL_ID;
import static no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations.MDC_CONSUMER_ID;
import static no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations.generateCallId;
import static no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations.putToMDC;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CALL_ID;

@Provider
public class MdcFilter implements ContainerRequestFilter {
    protected static final Logger log = LoggerFactory.getLogger(MdcFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) {

        String callId = Optional.ofNullable(requestContext.getHeaderString(HEADER_CALL_ID))
                .orElse(generateCallId());
        String consumerId = SubjectHandler.getConsumerId();
        String behandlingsId = requestContext.getUriInfo().getPathParameters().getFirst("behandlingsId");

        putToMDC(MDC_CALL_ID, callId);
        putToMDC(MDC_CONSUMER_ID, consumerId);
        putToMDC(MDC_BEHANDLINGS_ID, behandlingsId);
    }
}
