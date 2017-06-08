package no.nav.sbl.dialogarena.service.helpers.faktum;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.service.helpers.RegistryAwareHelper;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

@Component
public class HvisFlereErTrueHelper extends RegistryAwareHelper<String> {

    @Override
    public String getNavn() {
        return "hvisFlereErTrue";
    }

    @Override
    public String getBeskrivelse() {
        return "Finner alle fakta med key som begynner med teksten som sendes inn og teller om antallet med verdien true er større enn tallet som sendes inn.";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        Integer grense = Integer.parseInt((String) options.param(0));

        int size = (int)finnWebSoknad(options.context).getFaktaSomStarterMed(key).stream()
                .map(f -> f.getValue())
                .filter(f -> f != null)
                .filter(f -> f.equals("true"))
                .count();

        if (size > grense) {
            return options.fn();
        } else {
            return options.inverse();
        }
    }
}
