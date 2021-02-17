package no.nav.sosialhjelp.soknad.consumer.concurrency;

import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public final class RestCallUtils {

    private RestCallUtils() {

    }

    public static <T> T performRequestUsingContext(RestCallContext executionContext, Callable<T> callable) {
        final Map<String, String> mdcContextMap = MDC.getCopyOfContextMap();
        final Future<T> future = executionContext.getExecutorService().submit(() -> {
            try {
                if (mdcContextMap != null) {
                    MDC.setContextMap(mdcContextMap);
                }
                return callable.call();
            } finally {
                MDC.clear();
            }
        });

        try {
            return future.get(executionContext.getExecutorTimeoutInMilliseconds(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
