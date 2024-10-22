package no.nav.sosialhjelp.soknad.app.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.app.getBehandlingsId
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_BEHANDLINGS_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_CONSUMER_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_PATH
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.clearMDC
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.putToMDC
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class MdcFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val callId = request.getHeader(HEADER_CALL_ID) ?: MdcOperations.generateCallId()
        val consumerId = SubjectHandlerUtils.getConsumerId()
        val behandlingsId = request.getBehandlingsId()

        putToMDC(MDC_CALL_ID, callId)
        putToMDC(MDC_CONSUMER_ID, consumerId)
        behandlingsId?.let { putToMDC(MDC_BEHANDLINGS_ID, it) }
        putToMDC(MDC_PATH, request.requestURI)

        try {
            filterChain.doFilter(request, response)
        } finally {
            clearMDC()
        }
    }
}
