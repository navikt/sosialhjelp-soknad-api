package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;

import java.util.Locale;
import java.util.Properties;

public class TextService {

    private NavMessageSource navMessageSource = new NavMessageSource();

    public String getJsonOkonomiTittel(String key) {
        Properties properties = navMessageSource.getBundleFor("sendsoknad", new Locale("nb", "NO"));

        return properties.getProperty("json.okonomi." + key);
    }
}
