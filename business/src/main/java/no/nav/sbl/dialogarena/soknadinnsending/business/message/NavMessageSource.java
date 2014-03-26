package no.nav.sbl.dialogarena.soknadinnsending.business.message;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;
import java.util.Properties;


public class NavMessageSource extends ReloadableResourceBundleMessageSource {

    //Her er det mulig å legge til filtrering med prefix (hent bare properties med gitt nokkel)
    public Properties getBundleFor(String prefix, Locale locale) {
        return getMergedProperties(locale).getProperties();
    }
}
