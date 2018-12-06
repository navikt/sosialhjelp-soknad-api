package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class HvisSparingHelper extends RegistryAwareHelper<Object>{

    public static final String NAVN = "hvisSparing";

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {
        ArrayList<String> sparingNavn = new ArrayList<>();
        sparingNavn.add("brukskonto");
        sparingNavn.add("bsu");
        sparingNavn.add("sparekonto");
        sparingNavn.add("livsforsikringssparedel");
        sparingNavn.add("verdipapirer");
        sparingNavn.add("belop");
        if (key != null && sparingNavn.contains(key.toString())){
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
        return "Sjekker om en streng er av typen sparing";
    }
}
