package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ConcatHelper extends RegistryAwareHelper<String>{

    public static final String NAVN = "concat";
    public static final ConcatHelper INSTANS = new ConcatHelper();

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
        return "Legger sammen alle parametrene til tekststring";
    }

    @Override
    public CharSequence apply(String first, Options options) throws IOException {
        StringBuilder builder = new StringBuilder(first);
        for (Object string : options.params) {
            builder.append(string);
        }
        return builder.toString();
    }
}
