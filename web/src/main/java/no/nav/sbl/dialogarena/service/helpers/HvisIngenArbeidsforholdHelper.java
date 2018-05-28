package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;


@Component
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

        // Hvis det IKKE er slik at ((listen med arbeidsforholdfakta er tom) ELLER (det er slik at størrelsen på listen er 1 med et arbeidsforholdfaktum LIK NULL)), så har personen arbeid

        boolean harArbeid = !(arbeidsforholdFakta.isEmpty() || (arbeidsforholdFakta.size() == 1 && arbeidsforholdFakta.get(0).getValue() == null));


        if (!harArbeid) {
            return options.fn(this);
        } else {
            return options.inverse(this);
        }
    }

}


