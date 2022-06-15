package no.nav.sosialhjelp.soknad.client.config

import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.util.retry.Retry
import reactor.util.retry.RetryBackoffSpec
import java.time.Duration

object RetryUtils {
    private const val DEFAULT_MAX_ATTEMPTS: Long = 5
    private const val DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS: Long = 100

    val DEFAULT_RETRY_SERVER_ERRORS : RetryBackoffSpec =
        Retry
            .backoff(DEFAULT_MAX_ATTEMPTS, Duration.ofMillis(DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS))
            .filter { it is WebClientResponseException && it.statusCode.is5xxServerError }
}
