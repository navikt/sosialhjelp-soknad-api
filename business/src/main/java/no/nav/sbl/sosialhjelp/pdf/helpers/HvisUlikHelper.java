package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HvisUlikHelper extends RegistryAwareHelper<Object>{

    public static final String NAVN = "hvisUlik";

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {
        if (!(key != null && key.toString().equals(options.param(0)))) {
            return options.fn(this);
        } else {
            return options.inverse(this);
        }
    }

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Sjekker om to strenger er ulike";
    }
}
