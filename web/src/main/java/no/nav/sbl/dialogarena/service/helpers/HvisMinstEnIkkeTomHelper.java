package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HvisMinstEnIkkeTomHelper extends RegistryAwareHelper<Object>{

    public static final String NAVN = "hvisMinstEnIkkeTom";

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {
        for (Object val : options.params){
            if (val != null && !val.toString().isEmpty()) {
                return options.fn(this);
            }
        }
        if (key != null && !key.toString().isEmpty()) {
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
        return "Sjekker om minst ett av elementene inneholder noe";
    }
}
