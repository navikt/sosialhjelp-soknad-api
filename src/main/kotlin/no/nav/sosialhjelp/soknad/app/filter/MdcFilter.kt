package no.nav.sosialhjelp.soknad.app.filter

import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_BEHANDLINGS_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_CONSUMER_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.clearMDC
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.putToMDC
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class MdcFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val callId = request.getHeader(HEADER_CALL_ID) ?: MdcOperations.generateCallId()
        val consumerId = SubjectHandlerUtils.getConsumerId()
        val behandlingsId = getBehandlingsId(request)

        putToMDC(MDC_CALL_ID, callId)
        putToMDC(MDC_CONSUMER_ID, consumerId)
        behandlingsId?.let { putToMDC(MDC_BEHANDLINGS_ID, it) }

        try {
            filterChain.doFilter(request, response)
        } finally {
            clearMDC()
        }
    }

    private fun getBehandlingsId(request: HttpServletRequest): String? {
        val requestURI = request.requestURI
        if (requestURI.matches(Regex("^${SOKNAD_API_BASEURL}soknader/(.*)")) && !requestURI.matches(Regex("^${SOKNAD_API_BASEURL}soknader/opprettSoknad(.*)"))) {
            return requestURI.substringAfter("${SOKNAD_API_BASEURL}soknader/").substringBefore("/")
        }
        if (requestURI.matches(Regex("^${SOKNAD_API_BASEURL}innsendte/(.*)"))) {
            return requestURI.substringAfter("${SOKNAD_API_BASEURL}innsendte/")
        }
        if (requestURI.matches(Regex("^${SOKNAD_API_BASEURL}ettersendteVedlegg/(.*)"))) {
            return requestURI.substringAfter("${SOKNAD_API_BASEURL}ettersendteVedlegg/")
        }
        /*
        Skal matche disse:
        /opplastetVedlegg/{behandlingsId}/{vedleggId}/fil GET
        /opplastetVedlegg/{behandlingsId}/{type} POST
        /opplastetVedlegg/{behandlingsId}/{vedleggId} DELETE
        men ikke:
        /opplastetVedlegg/{vedleggId}/fil GET
         */
        if (request.method != "GET" && requestURI.matches(Regex("^${SOKNAD_API_BASEURL}opplastetVedlegg/(.*)"))) {
            return requestURI.substringAfter("${SOKNAD_API_BASEURL}opplastetVedlegg/").substringBefore("/")
        }
        if (request.method == "GET" && requestURI.matches(Regex("^${SOKNAD_API_BASEURL}opplastetVedlegg/(.*)/(.*)/fil"))) {
            return requestURI.substringAfter("${SOKNAD_API_BASEURL}opplastetVedlegg/").substringBefore("/")
        }
        return null
    }

    companion object {
        private const val SOKNAD_API_BASEURL = "/sosialhjelp/soknad-api/"
    }
}
