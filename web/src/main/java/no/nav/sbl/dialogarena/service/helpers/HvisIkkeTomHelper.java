package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HvisIkkeTomHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "hvisIkkeTom";

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {
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
        return "Dersom variabelen ikke er tom vil innholdet vises";
    }

}
