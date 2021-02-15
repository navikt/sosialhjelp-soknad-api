package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Locale;
import java.util.Properties;

@Component
public class TextService {

    @Inject
    private NavMessageSource navMessageSource;

    public String getJsonOkonomiTittel(String key) {
        Properties properties = navMessageSource.getBundleFor("sendsoknad", new Locale("nb", "NO"));

        return properties.getProperty("json.okonomi." + key);
    }
}