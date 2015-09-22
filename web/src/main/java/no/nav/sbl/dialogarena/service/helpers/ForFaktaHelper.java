package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;
import static no.nav.sbl.dialogarena.service.HandlebarsUtils.lagItererbarRespons;

@Component
public class ForFaktaHelper extends RegistryAwareHelper<String> {

    public static final String NAVN = "forFakta";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Finner alle fakta med en gitt key og setter hvert faktum som aktiv context etter tur. Har inverse ved ingen fakta.";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        List<Faktum> fakta = soknad.getFaktaMedKey(key);
        if (fakta.isEmpty()) {
            return options.inverse(this);
        } else {
            return lagItererbarRespons(options, fakta);
        }
    }
}
