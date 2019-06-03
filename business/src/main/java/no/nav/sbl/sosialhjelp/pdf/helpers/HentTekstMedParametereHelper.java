package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.sosialhjelp.pdf.CmsTekst;
import no.nav.sbl.sosialhjelp.pdf.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon.BUNDLE_NAME;
import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon.SOKNAD_TYPE_PREFIX;
import static no.nav.sbl.sosialhjelp.pdf.HandlebarContext.SPRAK;

@Component
public class HentTekstMedParametereHelper extends RegistryAwareHelper<String> {
    
    @Inject
    private NavMessageSource navMessageSource;

    private static final Logger LOG = LoggerFactory.getLogger(CmsTekst.class);

    @Override
    public String getNavn() {
        return "hentTekstMedParametere";
    }

    @Override
    public String getBeskrivelse() {
        return "Henter tekst fra cms, prøver med søknadens prefix + key, før den prøver med bare keyen. Kan sende inn parametere.";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        String tekst = this.getCmsTekst(key, options.params, SOKNAD_TYPE_PREFIX, BUNDLE_NAME, SPRAK, options);
        
        String nyTekst = UrlUtils.endreHyperLenkerTilTekst(tekst);

        if (tekst != null && !tekst.equals(nyTekst)) {
            tekst = nyTekst;
        }
        return tekst != null ? tekst : "";
    }

    private String getCmsTekst(String key, Object[] parameters, String soknadTypePrefix, String bundleName, Locale locale, Options options) {
        Properties bundle = navMessageSource.getBundleFor(bundleName, locale);

        String tekst = bundle.getProperty(soknadTypePrefix + "." + key);

        if (tekst == null) {
            tekst = bundle.getProperty(key);
        }

        if (tekst == null) {
            LOG.debug(String.format("Fant ikke tekst til oppsummering for nokkel %s i bundelen %s", key, bundleName));
            return tekst;
        } else {

            if (options.params.length%2 == 0) {
                for (int i = 0; i < options.params.length; i+=2) {
                    tekst = erstattTekst("{" + options.param(i) + "}", tekst, options.param(i+1).toString());
                }
            }
            return tekst;
        }
    }
    
    protected String erstattTekst(final String regex, final String input, final String replacement) {
        String[] lines = input.split("\n");
        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].replace(regex, replacement);
            
        }
        return Arrays.stream(lines).collect(Collectors.joining("\n"));
    }
}
