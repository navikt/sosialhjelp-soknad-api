package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sosialhjelp.soknad.business.pdf.CmsTekst;
import no.nav.sosialhjelp.soknad.business.pdf.UrlUtils;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Properties;

import static no.nav.sosialhjelp.soknad.business.pdf.HandlebarContext.SPRAK;
import static no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SosialhjelpInformasjon.BUNDLE_NAME;
import static no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SosialhjelpInformasjon.SOKNAD_TYPE_PREFIX;

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
    public CharSequence apply(String key, Options options) {
        String tekst = this.getCmsTekst(key, options);
        
        String nyTekst = UrlUtils.endreHyperLenkerTilTekst(tekst);

        if (tekst != null && !tekst.equals(nyTekst)) {
            tekst = nyTekst;
        }
        return tekst != null ? tekst : "";
    }

    private String getCmsTekst(String key, Options options) {
        Properties bundle = navMessageSource.getBundleFor(BUNDLE_NAME, SPRAK);

        String tekst = bundle.getProperty(SOKNAD_TYPE_PREFIX + "." + key);

        if (tekst == null) {
            tekst = bundle.getProperty(key);
        }

        if (tekst == null) {
            LOG.debug("Fant ikke tekst til oppsummering for nokkel {} i bundelen {}", key, BUNDLE_NAME);
        } else {

            if (options.params.length%2 == 0) {
                for (int i = 0; i < options.params.length; i+=2) {
                    tekst = erstattTekst("{" + options.param(i) + "}", tekst, options.param(i+1).toString());
                }
            }
        }
        return tekst;
    }
    
    private String erstattTekst(final String regex, final String input, final String replacement) {
        String[] lines = input.split("\n");
        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].replace(regex, replacement);
            
        }
        return String.join("\n", lines);
    }
}
