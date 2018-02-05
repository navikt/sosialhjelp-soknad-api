package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Map;

public final class JsonUtils {

    private JsonUtils() {

    }

    public static String nullWhenEmpty(Map<String, String> properties, String propertyName) {
        final String property = properties.get(propertyName);
        if (property == null || property.trim().equals("")) {
            return null;
        } else {
            return property;
        }
    }

    public static boolean isSystemProperties(Faktum faktum) {
        return faktum.getFaktumEgenskaper().stream().allMatch(e -> e.getSystemEgenskap() != null && e.getSystemEgenskap() == 1);
    }

    public static boolean isFaktumVerdi(WebSoknad webSoknad, String key) {
        final String verdi = webSoknad.getValueForFaktum(key);
        return verdi != null && Boolean.parseBoolean(verdi);
    }

    public static boolean empty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean nonEmpty(String s) {
        return !empty(s);
    }
}
