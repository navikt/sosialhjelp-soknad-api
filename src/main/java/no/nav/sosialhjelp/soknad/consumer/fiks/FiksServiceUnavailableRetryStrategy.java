package no.nav.sosialhjelp.soknad.consumer.fiks;

import io.vavr.collection.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.protocol.HttpContext;

import static org.apache.http.HttpStatus.SC_BAD_GATEWAY;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE;

public class FiksServiceUnavailableRetryStrategy implements ServiceUnavailableRetryStrategy {

    private static final int MAX_ATTEMPTS = 5;

    @Override
    public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
        var statusCode = response.getStatusLine().getStatusCode();

        // retry hvis statuscode er 500, 502 eller 503 og færre enn 5 retries er forsøkt
        return List.of(SC_INTERNAL_SERVER_ERROR, SC_BAD_GATEWAY, SC_SERVICE_UNAVAILABLE).contains(statusCode) && executionCount < MAX_ATTEMPTS;
    }

    @Override
    public long getRetryInterval() {
        return 200;
    }
}
