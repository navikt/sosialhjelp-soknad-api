package no.nav.sosialhjelp.soknad.common.mdc

import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.ext.Provider

@Provider
class MdcFilter : ContainerRequestFilter {
    override fun filter(requestContext: ContainerRequestContext) {
        val callId = requestContext.getHeaderString(HeaderConstants.HEADER_CALL_ID) ?: MdcOperations.generateCallId()
        val consumerId = SubjectHandlerUtils.getConsumerId()
        val behandlingsId = requestContext.uriInfo.pathParameters.getFirst("behandlingsId")

        MdcOperations.putToMDC(MdcOperations.MDC_CALL_ID, callId)
        MdcOperations.putToMDC(MdcOperations.MDC_CONSUMER_ID, consumerId)
        MdcOperations.putToMDC(MdcOperations.MDC_BEHANDLINGS_ID, behandlingsId)
    }
}
