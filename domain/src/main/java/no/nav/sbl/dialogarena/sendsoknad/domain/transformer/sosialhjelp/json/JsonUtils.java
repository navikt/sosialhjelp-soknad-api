package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Map;

public final class JsonUtils {

    private JsonUtils() {

    }

    public static String finnPropertyEllerNullOmTom(Map<String, String> properties, String propertyName) {
        final String property = properties.get(propertyName);
        if (property == null || property.trim().equals("")) {
            return null;
        } else {
            return property;
        }
    }

    public static boolean erAlleSystemProperties(Faktum faktum) {
        return faktum.getFaktumEgenskaper().stream().allMatch(e -> e.getSystemEgenskap() != null && e.getSystemEgenskap() == 1);
    }

    public static boolean faktumVerdiErTrue(WebSoknad webSoknad, String key) {
        final String verdi = webSoknad.getValueForFaktum(key);
        return verdi != null && Boolean.parseBoolean(verdi);
    }

    public static boolean erTom(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean erIkkeTom(String s) {
        return !erTom(s);
    }
}
