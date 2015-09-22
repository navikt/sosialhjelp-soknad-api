package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class HvisMerHelper extends RegistryAwareHelper<String>{

    public static final String NAVN = "hvisMer";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Evaluerer en string til double og sjekker om verdien er mer enn grenseverdien gitt ved andre inputparameter";
    }

    @Override
    public CharSequence apply(String value, Options options) throws IOException {
        try {
            Double grense = Double.parseDouble(((String) options.param(0)).replace(',', '.'));
            Double verdi = Double.parseDouble(value.replace(',', '.'));
            if (verdi > grense) {
                return options.fn(this);
            } else {
                return options.inverse(this);
            }
        } catch (NumberFormatException e) {
            getLogger(HvisMerHelper.class).error("Kunne ikke parse input til double", e);
            return options.fn(this);
        }
    }
}
