package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

@Component
public class ForFaktumHvisSantHelper extends RegistryAwareHelper<String> {

    @Override
    public String getNavn() {
        return "forFaktumHvisSant";
    }

    @Override
    public String getBeskrivelse() {
        return "Sjekker om faktumet til den innsendte keyen er sant eller ikke, setter faktumet som context";
    }

    @Override
    public CharSequence apply(String faktumKey, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        Faktum faktum = soknad.getFaktumMedKey(faktumKey);

        if (faktum != null && faktum.getValue() != null && faktum.getValue().equals("true")) {
            return options.fn(faktum);
        } else {
            return options.inverse(faktum);
        }
    }
}
