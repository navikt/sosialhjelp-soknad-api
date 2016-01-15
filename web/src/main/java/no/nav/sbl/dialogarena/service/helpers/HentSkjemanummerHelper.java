package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

@Component
public class HentSkjemanummerHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "hentSkjemanummer";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Setter inn søknadens skjemanummer, også om det er en søknad for dagpenger";
    }

    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        if (soknad.erDagpengeSoknad()) {
            return DagpengerUtils.getSkjemanummer(soknad);
        }
        return soknad.getskjemaNummer();
    }
}
