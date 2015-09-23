package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.service.CmsTekst;
import no.nav.sbl.dialogarena.service.HandlebarsUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

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
        return "Henter tekst fra cms, prøver med søknadens prefix + key, før den prøver med bare keyen";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        String soknadPrefix = HandlebarsUtils.finnWebSoknad(options.context).getSoknadPrefix();
        return cmsTekst.getCmsTekst(key, options.params, soknadPrefix);
    }
}
