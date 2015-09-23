package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.io.IOException;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

public class HvisEttersendingHelper extends RegistryAwareHelper<Object>{
    public static final String NAVN = "hvisEttersending";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Sjekk om søknad er av typen ettersending.";
    }

    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        if (soknad.erEttersending()) {
            return options.fn(this);
        } else {
            return options.inverse(this);
        }
    }

}
