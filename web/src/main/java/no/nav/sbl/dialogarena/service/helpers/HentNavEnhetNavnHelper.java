package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper.NavEnhet;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper.getNavEnhetFromWebSoknad;
import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

@Component
public class HentNavEnhetNavnHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "hentNavEnhetNavn";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Henter navnet på NAV enheten som bruker har valgt";
    }

    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        NavEnhet navEnhet = getNavEnhetFromWebSoknad(soknad);
        return navEnhet.getNavn();
    }
}
