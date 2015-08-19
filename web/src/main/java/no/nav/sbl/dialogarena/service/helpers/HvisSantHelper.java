package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HvisSantHelper extends RegistryAwareHelper<String>{

    public static final String NAVN = "hvisSant";
    public static final HvisSantHelper INSTANS = new HvisSantHelper();

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public Helper<String> getHelper() {
        return INSTANS;
    }

    @Override
    public String getBeskrivelse() {
        return "Dersom variabelen er \"true\" vil innholdet vises";
    }

    @Override
    public CharSequence apply(String value, Options options) throws IOException {
        if (value != null && value.equals("true")) {
            return options.fn(this);
        } else {
            return options.inverse(this);
        }
    }
}
