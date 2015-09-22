package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HvisMindreHelper extends RegistryAwareHelper<String> {

    public static final String NAVN = "hvisMindre";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Evaluerer en string til integer og sjekker om verdien er mindre enn andre inputparameter";
    }

    @Override
    public CharSequence apply(String value, Options options) throws IOException {
        Integer grense = Integer.parseInt((String) options.param(0));
        Integer verdi = Integer.parseInt(value);
        if (verdi < grense) {
            return options.fn(this);
        } else {
            return options.inverse(this);
        }
    }
}
