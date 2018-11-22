package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class HvisBarneutgiftHelper extends RegistryAwareHelper<Object>{

    public static final String NAVN = "hvisBarneutgift";

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {
        ArrayList<String> barneutgiftNavn = new ArrayList<>();
        barneutgiftNavn.add("barnFritidsaktiviteter");
        barneutgiftNavn.add("barnTannregulering");
        barneutgiftNavn.add("annenBarneutgift");
        barneutgiftNavn.add("barnebidrag");
        barneutgiftNavn.add("barnehage");
        barneutgiftNavn.add("sfo");
        if (key != null && barneutgiftNavn.contains(key.toString())){
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
        return "Sjekker om en streng er av typen barneutgift";
    }
}
