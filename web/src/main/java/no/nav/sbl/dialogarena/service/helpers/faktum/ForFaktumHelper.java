package no.nav.sbl.dialogarena.service.helpers.faktum;


import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.service.helpers.RegistryAwareHelper;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

@Component
public class ForFaktumHelper extends RegistryAwareHelper<String> {

    public static final String NAVN = "forFaktum";

    @Override
    public CharSequence apply(String o, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        Faktum faktum = soknad.getFaktumMedKey(o);

        if (faktum == null || (faktum.getValue() == null && faktum.getProperties().isEmpty())) {
            return options.inverse(this);
        } else {
            return options.fn(faktum);
        }
    }

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Finner et faktum og setter det som aktiv context. Har også inverse om faktum ikke finnes. ";
    }
}
