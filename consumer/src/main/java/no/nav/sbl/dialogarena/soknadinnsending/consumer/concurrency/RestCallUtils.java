package no.nav.sbl.dialogarena.soknadinnsending.consumer.concurrency;

import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.*;


public final class RestCallUtils {
    
    private RestCallUtils() {
        
    }

        
    public static <T> T performRequestUsingContext(RestCallContext executionContext, Callable<T> callable) {
        Map<String, String> mdcContextMap = MDC.getCopyOfContextMap();
        Future<T> future = executionContext.getExecutorService().submit(() -> {
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
        } catch (TimeoutException|ExecutionException|InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
