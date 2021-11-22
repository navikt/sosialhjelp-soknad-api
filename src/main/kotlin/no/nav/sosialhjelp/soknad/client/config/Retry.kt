package no.nav.sosialhjelp.soknad.client.config

object RetryUtils {
    const val DEFAULT_MAX_ATTEMPTS = 5
    const val DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS: Long = 100
    const val DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER = 2.0
}
