package no.nav.sosialhjelp.soknad.common.rest

/* Originally from common-java-modules (no.nav.sbl.dialogarena.common.rest) */

import no.nav.sosialhjelp.metrics.MetricsFactory
import no.nav.sosialhjelp.metrics.Timer
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.Optional.ofNullable
import java.util.concurrent.TimeUnit
import javax.ws.rs.client.ClientRequestContext
import javax.ws.rs.client.ClientRequestFilter
import javax.ws.rs.client.ClientResponseContext
import javax.ws.rs.client.ClientResponseFilter
import javax.ws.rs.core.Cookie
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.UriBuilder

class ClientLogFilter(
    private val filterConfig: ClientLogFilterConfig
) : ClientResponseFilter, ClientRequestFilter {

    override fun filter(clientRequestContext: ClientRequestContext) {
        log.info("${clientRequestContext.method} ${uriForLogging(clientRequestContext)}")
        val requestHeaders = clientRequestContext.headers

        // jersey-client generates cookies in org.glassfish.jersey.message.internal.CookieProvider according to the
        // deprecated rfc2109 specification, which prefixes the cookie with its version. This may not be supported by modern servers.
        // Therefore we serialize cookies on the more modern and simpler rfc6265-format
        // https://www.ietf.org/rfc/rfc2109.txt
        // https://tools.ietf.org/html/rfc6265
        requestHeaders.replace(
            HttpHeaders.COOKIE,
            listOf(
                requestHeaders[HttpHeaders.COOKIE]
                    ?.map { toCookieString(it) }
                    ?.joinToString(separator = "; ") { it }
            )
        )
        if (!filterConfig.disableMetrics) {
            val timer = MetricsFactory.createTimer(filterConfig.metricName)
            timer.start()
            clientRequestContext.setProperty(name, Data(timer))
        }
    }

    private fun toCookieString(cookie: Any): String {
        return when (cookie) {
            is String -> cookie
            is Cookie -> "${cookie.name}=${cookie.value}"
            else -> throw IllegalArgumentException()
        }
    }

    override fun filter(clientRequestContext: ClientRequestContext, clientResponseContext: ClientResponseContext) {
        requestComplete(clientRequestContext, clientResponseContext.status, null)
    }

    fun requestFailed(request: ClientRequestContext, throwable: Throwable) {
        log.warn(throwable.message, throwable)
        requestComplete(request, 520, throwable)
    }

    private fun requestComplete(clientRequestContext: ClientRequestContext, status: Int, throwable: Throwable?) {
        if (!filterConfig.disableMetrics) {
            val data = clientRequestContext.getProperty(name) as Data
            val timer = data.timer
            val uri = clientRequestContext.uri
            val host = uri.host
            timer
                .stop()
                .addFieldToReport("httpStatus", status)
                .addFieldToReport("host", host)
                .addFieldToReport("path", uri.path)
                .report()
            MetricsFactory.getMeterRegistry()
                .timer(
                    "rest_client",
                    "host",
                    host,
                    "status",
                    status.toString(),
                    "error",
                    ofNullable(throwable)
                        .map { ExceptionUtils.getRootCause(it) }
                        .map { it.javaClass.simpleName }
                        .orElse("")
                )
                .record(System.currentTimeMillis() - data.invocationTimestamp, TimeUnit.MILLISECONDS)
        }
    }

    private fun uriForLogging(clientRequestContext: ClientRequestContext): URI {
        val uri = clientRequestContext.uri
        return if (filterConfig.disableParameterLogging) UriBuilder.fromUri(uri).replaceQuery("").build() else uri
    }

    data class ClientLogFilterConfig(
        val metricName: String? = null,
        val disableMetrics: Boolean = false,
        val disableParameterLogging: Boolean = false
    )

    private data class Data(
        val timer: Timer
    ) {
        val invocationTimestamp: Long = System.currentTimeMillis()
    }

    companion object {
        private val log = LoggerFactory.getLogger(ClientLogFilter::class.java)
        private val name = ClientLogFilter::class.java.name
    }
}
