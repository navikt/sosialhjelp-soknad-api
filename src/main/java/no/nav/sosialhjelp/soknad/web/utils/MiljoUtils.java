package no.nav.sosialhjelp.soknad.web.utils;

public final class MiljoUtils {

    private MiljoUtils() {
    }

    private static final String NAIS_APP_IMAGE = "NAIS_APP_IMAGE";
    private static final String NAIS_APP_NAME = "NAIS_APP_NAME";

    public static String getNaisAppImage() {
        return getenv(NAIS_APP_IMAGE, "version");
    }

    public static String getNaisAppName() {
        return getenv(NAIS_APP_NAME, "sosialhjelp-soknad-api");
    }


    private static String getenv(String env, String defaultValue) {
        try {
            return System.getenv(env);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
