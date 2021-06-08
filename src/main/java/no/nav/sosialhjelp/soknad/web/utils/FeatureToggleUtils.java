package no.nav.sosialhjelp.soknad.web.utils;

public class FeatureToggleUtils {

    public static boolean enableModalV2(String uid) {
        if (uid == null) return false;
        return Long.parseLong(uid) % 2 == 0;
    }
}
