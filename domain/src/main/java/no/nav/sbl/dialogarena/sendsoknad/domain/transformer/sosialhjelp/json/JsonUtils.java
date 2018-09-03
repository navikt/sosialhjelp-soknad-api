package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static java.lang.Double.parseDouble;
import static java.lang.Math.round;
import static org.apache.commons.lang3.StringUtils.deleteWhitespace;

public final class JsonUtils {

    private JsonUtils() {

    }

    public static String finnPropertyEllerNullOmTom(Map<String, String> properties, String propertyName) {
        final String property = properties.get(propertyName);
        if (erTom(property)) {
            return null;
        } else {
            return property;
        }
    }
    
    public static String finnPropertyEllerTom(Map<String, String> properties, String propertyName) {
        final String s = finnPropertyEllerNullOmTom(properties, propertyName);
        return s != null ? s : "";
    }

    public static boolean erAlleSystemProperties(Faktum faktum) {
        return faktum.getFaktumEgenskaper().stream().allMatch(e -> e.getSystemEgenskap() != null && e.getSystemEgenskap() == 1);
    }

    public static boolean faktumVerdiErTrue(WebSoknad webSoknad, String key) {
        final String verdi = webSoknad.getValueForFaktum(key);
        return verdi != null && Boolean.parseBoolean(verdi);
    }


    public static int tilInt(String s) {
        if (erTom(s)) {
            return 0;
        }
        return Integer.parseInt(s);
    }
    
    public static Integer tilInteger(String s) {
        if (erTom(s)) {
            return null;
        }

        return Integer.valueOf(s);
    }

    public static Integer tilIntegerMedAvrunding(String s) {
        if (erTom(s)) {
            return null;
        }
        s = s.replaceAll(",", ".");
        s = s.replaceAll("\u00A0", "");
        double d = parseDouble(deleteWhitespace(s));
        return (int) round(d);
    }

    public static boolean erTom(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static boolean erIkkeTom(String s) {
        return !erTom(s);
    }
}
