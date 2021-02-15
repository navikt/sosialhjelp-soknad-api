package no.nav.sbl.dialogarena.retry;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import org.slf4j.Logger;

import java.time.Duration;

public final class RetryUtils {

    public static final int DEFAULT_MAX_ATTEMPTS = 5;
    public static final long DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS = 100;
    public static final double DEFAULT_EXPONENTIAL_BACKOFF_MULTIPLIER = 2.0;

    // For å hindre instansiering
    private RetryUtils() {
    }

    public static Retry retryConfig(
            String baseUrl,
            int maxAttempts,
            long initialWaitIntervalMillis,
            double exponentialBackoffMultiplier,
            Class<? extends Throwable>[] retryableExceptions,
            Logger log
    ) {
        var retryConfig = RetryConfig.custom()
                .retryExceptions(retryableExceptions)
                .maxAttempts(maxAttempts)
                .waitDuration(Duration.ofMillis(initialWaitIntervalMillis))
                .intervalFunction(IntervalFunction.ofExponentialBackoff(initialWaitIntervalMillis, exponentialBackoffMultiplier))
                .build();
        var retry = RetryRegistry.of(retryConfig)
                .retry(baseUrl);

        retry.getEventPublisher()
                .onRetry(event -> log.warn("Retry client med baseUrl={}. Forsøk nr {} av {}. Feil: {} ({})",
                        baseUrl,
                        event.getNumberOfRetryAttempts(),
                        maxAttempts,
                        event.getLastThrowable().getClass().getSimpleName(),
                        event.getLastThrowable().getMessage()))
                .onSuccess(event -> {
                    if (event.getNumberOfRetryAttempts() > 1) {
                        log.info("Retry client med baseUrl={}. Forsøk nr {} av {} -> suksess",
                                baseUrl,
                                event.getNumberOfRetryAttempts(),
                                maxAttempts);
                    }
                })
                .onError(event -> log.warn("Retry client med baseUrl={}. Maks antall retries nådd ({}). Feil: {} ({})",
                        baseUrl,
                        event.getNumberOfRetryAttempts(),
                        event.getLastThrowable().getClass().getSimpleName(),
                        event.getLastThrowable().getMessage(),
                        event.getLastThrowable()));
        return retry;
    }

    public static <T> T withRetry(Retry retry, CheckedFunction0<T> supplier) {
        return Try.of(Retry.decorateCheckedSupplier(retry, supplier)).get();
    }

}
