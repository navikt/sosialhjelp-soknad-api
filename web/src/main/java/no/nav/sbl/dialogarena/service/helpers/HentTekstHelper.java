package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.*;
import no.nav.sbl.dialogarena.service.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.*;
import org.springframework.stereotype.*;

import javax.inject.*;
import java.io.*;

import static org.apache.commons.lang3.LocaleUtils.*;

@Component
public class HentTekstHelper extends RegistryAwareHelper<String> {

    @Inject
    private CmsTekst cmsTekst;

    @Override
    public String getNavn() {
        return "hentTekst";
    }

    @Override
    public String getBeskrivelse() {
        return "Henter tekst fra cms, prøver med søknadens prefix + key, før den prøver med bare keyen. Kan sende inn parametere.";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        WebSoknad soknad = HandlebarsUtils.finnWebSoknad(options.context);
        Faktum sprakFaktum = soknad.getFaktumMedKey("skjema.sprak");
        String sprak = sprakFaktum == null ? "nb_NO" : sprakFaktum.getValue();
        return cmsTekst.getCmsTekst(key, options.params, soknad.getSoknadPrefix(), toLocale(sprak));
    }
}
