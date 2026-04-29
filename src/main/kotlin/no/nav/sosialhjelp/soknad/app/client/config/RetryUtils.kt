package no.nav.sosialhjelp.soknad.app.client.config

import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.util.retry.Retry
import reactor.util.retry.RetryBackoffSpec
import java.time.Duration

object RetryUtils {
    private const val DEFAULT_MAX_ATTEMPTS: Long = 3
    private const val DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS: Long = 300

    val DEFAULT_RETRY_SERVER_ERRORS: RetryBackoffSpec =
        Retry
            .backoff(DEFAULT_MAX_ATTEMPTS, Duration.ofMillis(DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS))
            .filter { it.shouldRetry() }

    private fun Throwable.shouldRetry(): Boolean =
        when (this) {
            is WebClientResponseException -> !this.statusCode.is4xxClientError
            is WebClientRequestException -> true
            else -> false
        }
}
