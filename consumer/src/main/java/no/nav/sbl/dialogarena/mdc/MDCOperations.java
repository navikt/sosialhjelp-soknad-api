package no.nav.sbl.dialogarena.mdc;

/* Copy from modig-log-common */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.security.SecureRandom;

/**
 * Utility-klasse for kommunikasjon med MDC.
 */
public final class MDCOperations {
    protected static final Logger log = LoggerFactory.getLogger(MDCOperations.class.getName());

    public static final String MDC_CALL_ID = "callId";

    private static final SecureRandom RANDOM = new SecureRandom();

    private MDCOperations() {
    }

    public static String generateCallId() {
        int randomNr = getRandomNumber();
        long systemTime = getSystemTime();

        StringBuilder callId = new StringBuilder();
        callId.append("CallId_");
        callId.append(systemTime);
        callId.append("_");
        callId.append(randomNr);

        return callId.toString();
    }

    public static String getFromMDC(String key) {
        String value = MDC.get(key);
        log.debug("Getting key: " + key + " from MDC with value: " + value);
        return value;
    }

    public static void putToMDC(String key, String value) {
        log.debug("Putting value: " + value + " on MDC with key: " + key);
        MDC.put(key, value);
    }

    public static void remove(String key) {
        log.debug("Removing key: " + key);
        MDC.remove(key);
    }

    private static int getRandomNumber() {
        int value = RANDOM.nextInt(Integer.MAX_VALUE);
        return value;
    }

    private static long getSystemTime() {
        return System.currentTimeMillis();
    }
}
