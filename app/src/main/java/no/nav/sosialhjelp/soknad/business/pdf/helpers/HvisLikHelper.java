package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HvisLikHelper extends RegistryAwareHelper<Object>{

    public static final String NAVN = "hvisLik";

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {
        for (Object param : options.params) {
            if (key != null && key.toString().equals(param)) {
                return options.fn(this);
            }
        }
        return options.inverse(this);
    }

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Sjekker om to strenger er like";
    }
}
