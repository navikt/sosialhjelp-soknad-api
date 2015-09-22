package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ToCapitalizedHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "toCapitalized";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Gjør om en tekst til at alle ord starter med store bokstaver";
    }

    @Override
    public CharSequence apply(Object value, Options options) throws IOException {
        if (value == null) {
            return "";
        }
        return WordUtils.capitalizeFully(value.toString());
    }
}
