package no.nav.sosialhjelp.soknad.innsending.digisosapi

import org.apache.http.HttpResponse
import org.apache.http.HttpStatus.SC_BAD_GATEWAY
import org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
import org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE
import org.apache.http.client.ServiceUnavailableRetryStrategy
import org.apache.http.protocol.HttpContext

class FiksServiceUnavailableRetryStrategy : ServiceUnavailableRetryStrategy {

    override fun retryRequest(response: HttpResponse, executionCount: Int, context: HttpContext): Boolean {
        val statusCode = response.statusLine.statusCode

        // retry hvis statuscode er 500, 502 eller 503 og færre enn 5 retries er forsøkt
        return listOf(SC_INTERNAL_SERVER_ERROR, SC_BAD_GATEWAY, SC_SERVICE_UNAVAILABLE)
            .contains(statusCode) && executionCount < MAX_ATTEMPTS
    }

    override fun getRetryInterval(): Long {
        return 200
    }

    companion object {
        private const val MAX_ATTEMPTS = 5
    }
}
