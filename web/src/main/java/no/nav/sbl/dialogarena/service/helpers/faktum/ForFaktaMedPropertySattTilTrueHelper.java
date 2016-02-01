package no.nav.sbl.dialogarena.service.helpers.faktum;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.service.helpers.RegistryAwareHelper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;
import static no.nav.sbl.dialogarena.service.HandlebarsUtils.lagItererbarRespons;

@Component
public class ForFaktaMedPropertySattTilTrueHelper extends RegistryAwareHelper<String> {

    public static final String NAVN = "forFaktaMedPropertySattTilTrue";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Finner alle fakta med gitt key som har gitt property satt til true";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        List<Faktum> fakta = soknad.getFaktaMedKeyOgPropertyLikTrue(key, (String) options.param(0));
        if (fakta.isEmpty()) {
            return options.inverse(this);
        } else {
            return lagItererbarRespons(options, fakta);
        }
    }
}