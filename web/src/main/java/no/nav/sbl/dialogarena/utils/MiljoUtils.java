package no.nav.sbl.dialogarena.utils;

public final class MiljoUtils {

    private MiljoUtils() {
    }

    private static final String NAIS_APP_IMAGE = "NAIS_APP_IMAGE";

    public static String getAppImage() {
        return getEnvVariable(NAIS_APP_IMAGE, "version");
    }

    private static String getEnvVariable(String key, String defaultValue) {
        try {
            var env = System.getenv(key);
            if (env == null) {
                return defaultValue;
            }
            return env;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
