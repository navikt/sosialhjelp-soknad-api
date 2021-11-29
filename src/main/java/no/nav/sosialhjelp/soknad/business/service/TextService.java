package no.nav.sosialhjelp.soknad.business.service;

import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Properties;

@Component
public class TextService {

    private final NavMessageSource navMessageSource;

    public TextService(NavMessageSource navMessageSource) {
        this.navMessageSource = navMessageSource;
    }

    public String getJsonOkonomiTittel(String key) {
        Properties properties = navMessageSource.getBundleFor("sendsoknad", new Locale("nb", "NO"));

        return properties.getProperty("json.okonomi." + key);
    }
}