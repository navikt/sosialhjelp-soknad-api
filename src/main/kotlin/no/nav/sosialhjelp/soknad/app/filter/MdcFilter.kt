package no.nav.sosialhjelp.soknad.app.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_HTTP_METHOD
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_PATH
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_REFERER
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.MDC_SOKNAD_ID
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.clearMDC
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.putToMDC
import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import tools.jackson.module.kotlin.jacksonObjectMapper

@Component
class MdcFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val soknadId = getSoknadId(request) ?: getBehandlingsId(request)

        soknadId?.let { putToMDC(MDC_SOKNAD_ID, it) }
        putToMDC(MDC_HTTP_METHOD, request.method)
        putToMDC(MDC_PATH, request.requestURI)
        request.getHeader(HttpHeaders.REFERER)?.let { putToMDC(MDC_REFERER, it) }

        try {
            filterChain.doFilter(request, response)
        } finally {
            clearMDC()
        }
    }

    private fun getSoknadId(request: HttpServletRequest): String? {
        return request.requestURI
            ?.let {
                if (
                    it.matches(Regex("^${SOKNAD_API_BASEURL}soknad/(.*)")) &&
                    !it.matches(Regex("^${SOKNAD_API_BASEURL}soknad/create"))
                ) {
                    return it.substringAfter("${SOKNAD_API_BASEURL}soknad/").substringBefore("/")
                }
                null
            }
    }

    private fun getBehandlingsId(request: HttpServletRequest): String? {
        val requestURI = request.requestURI
        if (requestURI.matches(Regex("^${SOKNAD_API_BASEURL}soknader/(.*)")) &&
            !requestURI.matches(Regex("^${SOKNAD_API_BASEURL}soknader/opprettSoknad(.*)"))
        ) {
            return requestURI.substringAfter("${SOKNAD_API_BASEURL}soknader/").substringBefore("/")
        }
        if (requestURI.matches(Regex("^${SOKNAD_API_BASEURL}innsendte/(.*)"))) {
            return requestURI.substringAfter("${SOKNAD_API_BASEURL}innsendte/")
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

// Kopierer MDC-context inn til reactor threads
object MdcExchangeFilter : ExchangeFilterFunction {
    override fun filter(
        request: ClientRequest,
        next: ExchangeFunction,
    ): Mono<ClientResponse> {
        val copy = MDC.getCopyOfContextMap()
        logger.info("Thread: ${getThreadName()}, MDC Copy: ${jacksonObjectMapper().writeValueAsString(copy)}")

        var currentOldContextMap: Map<String, String>? = null

        return next
            .exchange(request)
            .doOnEach {
                currentOldContextMap = MDC.getCopyOfContextMap() ?: emptyMap()

                val combinedMap = currentOldContextMap + (copy ?: emptyMap())
                MDC.setContextMap(combinedMap)
            }
            .doFinally {
                logger.info("Thread: ${getThreadName()}, Do finally: ${jacksonObjectMapper().writeValueAsString(currentOldContextMap)}")
                MDC.setContextMap(currentOldContextMap)
            }
    }

    private val logger by logger()
}

private fun getThreadName(): String = Thread.currentThread().name
