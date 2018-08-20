package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.TekstStruktur;
import no.nav.sbl.dialogarena.service.oppsummering.OppsummeringsFaktum;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;
import static no.nav.sbl.dialogarena.service.HandlebarsUtils.getOppsummeringsFaktum;

@Component
public class HvisFaktumstrukturHarInfoteksterHelper extends RegistryAwareHelper<Object>{

    public static final String NAVN = "hvisFaktumstrukturHarInfotekster";

    @Override
    public CharSequence apply(Object obj, Options options) throws IOException {
        OppsummeringsFaktum oppsummeringsFaktum = getOppsummeringsFaktum(options.context);
        WebSoknad webSoknad = finnWebSoknad(options.context);

        if (oppsummeringsFaktum != null) {
            List<TekstStruktur> infotekster = oppsummeringsFaktum.struktur.getInfotekster(webSoknad, oppsummeringsFaktum.faktum);
            if (infotekster == null || infotekster.isEmpty()) {
                return options.inverse();
            }
            return options.fn(this);
        } else {
            return options.inverse(this);
        }
    }

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Sjekker om man har definert infotekster på faktumstrukturen for faktum på context";
    }
}
