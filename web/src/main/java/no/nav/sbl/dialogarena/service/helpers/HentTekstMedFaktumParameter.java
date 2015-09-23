package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.service.CmsTekst;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

@Component
public class HentTekstMedFaktumParameter extends RegistryAwareHelper<String> {

    @Inject
    private CmsTekst cmsTekst;

    @Override
    public String getNavn() {
        return "hentTekstMedFaktumParameter";
    }

    @Override
    public String getBeskrivelse() {
        return "Henter tekst fra cms for en gitt key, med verdien til et faktum som parameter. Faktumet hentes basert på key";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        Faktum faktum = soknad.getFaktumMedKey(options.param(0).toString());
        String prefix = soknad.getSoknadPrefix();

        return cmsTekst.getCmsTekst(key, new Object[]{faktum.getValue()}, prefix);
    }
}
