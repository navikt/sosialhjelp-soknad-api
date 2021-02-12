package no.nav.sbl.sosialhjelp.pdf;

import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;

@Component
public class CmsTekst {
    @Inject
    private NavMessageSource navMessageSource;

    private static final Logger LOG = LoggerFactory.getLogger(CmsTekst.class);


    public String getCmsTekst(String key, Object[] parameters, String soknadTypePrefix, String bundleName, Locale locale) {
        Properties bundle = navMessageSource.getBundleFor(bundleName, locale);

        String tekst = bundle.getProperty(soknadTypePrefix + "." + key);

        if (tekst == null) {
            tekst = bundle.getProperty(key);
        }

        if (tekst == null) {
            LOG.debug("Fant ikke tekst til oppsummering for nokkel {} i bundelen {}", key, bundleName);
            return tekst;
        } else {
            return MessageFormat.format(tekst, parameters);
        }
    }
}