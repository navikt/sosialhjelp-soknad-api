package no.nav.sosialhjelp.soknad.common.mdc

import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.ext.Provider

@Provider
class MdcFilter : ContainerRequestFilter {
    override fun filter(requestContext: ContainerRequestContext) {
        val callId = requestContext.getHeaderString(HeaderConstants.HEADER_CALL_ID) ?: MDCOperations.generateCallId()
        val consumerId = SubjectHandlerUtils.getConsumerId()
        val behandlingsId = requestContext.uriInfo.pathParameters.getFirst("behandlingsId")

        MDCOperations.putToMDC(MDCOperations.MDC_CALL_ID, callId)
        MDCOperations.putToMDC(MDCOperations.MDC_CONSUMER_ID, consumerId)
        MDCOperations.putToMDC(MDCOperations.MDC_BEHANDLINGS_ID, behandlingsId)
    }
}
