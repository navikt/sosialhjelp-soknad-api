package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Locale;
import java.util.Properties;

@Component
public class CmsTekst {
    @Inject
    private NavMessageSource messageSource;

    public String getCmsTekst(String key, Object[] parameters, String soknadTypePrefix, String bundleName, Locale locale) {
        Properties bundle = messageSource.getBundleFor(bundleName, locale);

        String tekst = bundle.getProperty(soknadTypePrefix + "." + key);

        if (tekst == null) {
            tekst = bundle.getProperty(key);
        }

        return tekst;
    }
}
