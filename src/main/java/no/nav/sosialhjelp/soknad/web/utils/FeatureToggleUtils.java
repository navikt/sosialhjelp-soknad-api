package no.nav.sosialhjelp.soknad.web.utils;

import static org.apache.commons.lang3.StringUtils.isNumeric;

public class FeatureToggleUtils {

    public static boolean enableModalV2(String uid) {
        if (!isNumeric(uid)) return false;
        return Long.parseLong(uid) % 2 == 0;
    }
}
