package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class HvisBoutgiftHelper extends RegistryAwareHelper<Object>{

    public static final String NAVN = "hvisBoutgift";

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {
        ArrayList<String> boutgiftNavn = new ArrayList<>();
        boutgiftNavn.add("strom");
        boutgiftNavn.add("kommunalAvgift");
        boutgiftNavn.add("oppvarming");
        boutgiftNavn.add("annenBoutgift");
        boutgiftNavn.add("husleie");
        boutgiftNavn.add("boliglanAvdrag");
        boutgiftNavn.add("boliglanRenter");
        if (key != null && boutgiftNavn.contains(key.toString())){
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
        return "Sjekker om en streng er av typen boutgift";
    }
}
