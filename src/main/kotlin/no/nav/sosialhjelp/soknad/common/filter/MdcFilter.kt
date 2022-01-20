package no.nav.sosialhjelp.soknad.common.filter

import no.nav.sosialhjelp.soknad.common.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations.MDC_BEHANDLINGS_ID
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations.MDC_CONSUMER_ID
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations.putToMDC
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.ext.Provider

@Provider
class MdcFilter : ContainerRequestFilter {
    override fun filter(requestContext: ContainerRequestContext) {
        val callId = requestContext.getHeaderString(HEADER_CALL_ID) ?: MdcOperations.generateCallId()
        val consumerId = SubjectHandlerUtils.getConsumerId()
        val behandlingsId = requestContext.uriInfo.pathParameters.getFirst("behandlingsId")

        putToMDC(MDC_CALL_ID, callId)
        putToMDC(MDC_CONSUMER_ID, consumerId)
        putToMDC(MDC_BEHANDLINGS_ID, behandlingsId)
    }
}
