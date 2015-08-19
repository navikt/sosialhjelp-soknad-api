package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ToLowerCaseHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "toLowerCase";
    public static final ToLowerCaseHelper INSTANS = new ToLowerCaseHelper();

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public Helper<Object> getHelper() {
        return INSTANS;
    }

    @Override
    public String getBeskrivelse() {
        return "Gjør om en tekst til kun små bokstaver";
    }

    @Override
    public CharSequence apply(Object value, Options options) throws IOException {
        return value.toString().toLowerCase();
    }
}
