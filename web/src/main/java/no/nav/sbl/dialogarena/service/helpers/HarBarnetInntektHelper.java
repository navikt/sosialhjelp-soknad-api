package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnFaktum;
import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

@Component
public class HarBarnetInntektHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "harBarnetInntekt";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Henter summen hvis barnet har inntekt. Må brukes innenfor en #forFaktum eller #forFakta helper. ";
    }

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {

        WebSoknad soknad = finnWebSoknad(options.context);
        Faktum parentFaktum = finnFaktum(options.context);

        Faktum harInntekt = soknad.getFaktaMedKeyOgParentFaktum("barn.harinntekt", parentFaktum.getFaktumId()).get(0);

        if (harInntekt != null && "true".equals(harInntekt.getValue())) {
            Faktum sumInntekt = soknad.getFaktaMedKeyOgParentFaktum("barn.inntekt", parentFaktum.getFaktumId()).get(0);
            return options.fn(sumInntekt);
        } else {
            return options.inverse(this);
        }
    }
}
