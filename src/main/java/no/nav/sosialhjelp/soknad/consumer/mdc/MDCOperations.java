package no.nav.sosialhjelp.soknad.consumer.mdc;

/* Copy from modig-log-common */

import org.slf4j.MDC;

import java.security.SecureRandom;

/**
 * Utility-klasse for kommunikasjon med MDC.
 */
public final class MDCOperations {
    public static final String MDC_CALL_ID = "callId";
    public static final String MDC_BEHANDLINGS_ID = "behandlingsId";
    public static final String MDC_CONSUMER_ID = "consumerId";

    private static final SecureRandom RANDOM = new SecureRandom();

    private MDCOperations() {
    }

    public static String generateCallId() {
        int randomNr = getRandomNumber();
        long systemTime = getSystemTime();

        return String.format("CallId_%s_%s", systemTime, randomNr);
    }

    public static String getFromMDC(String key) {
        return MDC.get(key);
    }

    public static void putToMDC(String key, String value) {
        MDC.put(key, value);
    }

    public static void remove(String key) {
        MDC.remove(key);
    }

    private static int getRandomNumber() {
        return RANDOM.nextInt(Integer.MAX_VALUE);
    }

    private static long getSystemTime() {
        return System.currentTimeMillis();
    }
}
