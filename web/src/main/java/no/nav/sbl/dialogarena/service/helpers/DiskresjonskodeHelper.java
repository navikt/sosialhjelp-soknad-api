package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.service.HandleBarKjoerer;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.io.IOException;

public class DiskresjonskodeHelper implements Helper<Object> {

    public static final Helper<Object> INSTANCE = new DiskresjonskodeHelper();

    public static final String NAME = "hvisKode6Eller7";

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {
        WebSoknad soknad = HandleBarKjoerer.finnWebSoknad(options.context);
        String kode = soknad.getFaktumMedKey("personalia").getProperties().get("diskresjonskode");

        if (kode != null && (kode.equals("6") || kode.equals("7"))) {
            return options.fn(this);
        } else {
            return options.inverse(this);
        }
    }
}
