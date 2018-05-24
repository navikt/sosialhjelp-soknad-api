package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.io.IOException;
import java.util.List;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

public class HvisIngenArbeidsforholdHelper extends RegistryAwareHelper<Object> {
    public static final String NAVN = "hvisIngenArbeidsforhold";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Sjekk om søker har noen arbeidsforhold.";
    }

    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);


        List<Faktum> arbeidsforholdFakta = soknad.getFaktaMedKey("arbeidsforhold");

        boolean harArbeid = !arbeidsforholdFakta.isEmpty();

        if (!harArbeid) {
            return options.fn(this);
        } else {
            return options.inverse(this);
        }
    }

}


