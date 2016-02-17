package no.nav.sbl.dialogarena.service.helpers.faktum;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.service.helpers.RegistryAwareHelper;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

@Component
public class ForFaktumMedIdHelper extends RegistryAwareHelper<String> {

    public static final String NAVN = "forFaktumMedId";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Returnerer et faktum med den gitte ID-en";
    }

    @Override
    public CharSequence apply(String s, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        Faktum faktum = soknad.getFaktumMedId(s);

        if (faktum == null || (faktum.getValue() == null && faktum.getProperties().isEmpty())) {
            return options.inverse(this);
        } else {
            return options.fn(faktum);
        }
    }
}