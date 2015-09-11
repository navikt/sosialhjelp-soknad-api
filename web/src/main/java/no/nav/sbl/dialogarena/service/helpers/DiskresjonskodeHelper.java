package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.service.HandleBarKjoerer;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DiskresjonskodeHelper extends RegistryAwareHelper<Object> {

    public static final Helper<Object> INSTANS = new DiskresjonskodeHelper();

    public static final String NAVN = "hvisHarDiskresjonskode";
    public static final String DISKRESJONSKODE_PROPERTY = "diskresjonskode";
    public static final String PERSONALIA_FAKTUM_KEY = "personalia";

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {
        WebSoknad soknad = HandleBarKjoerer.finnWebSoknad(options.context);
        String kode = soknad.getFaktumMedKey(PERSONALIA_FAKTUM_KEY).getProperties().get(DISKRESJONSKODE_PROPERTY);

        if (kode != null && (kode.equals("6") || kode.equals("7"))) {
            return options.fn(this);
        } else {
            return options.inverse(this);
        }
    }

    @Override
    public String getNavn() { return NAVN; }

    @Override
    public Helper<Object> getHelper() { return INSTANS; }

    @Override
    public String getBeskrivelse() {
        return "Viser innhold avhengig av om " +
                "personalia indikerer diskresjonskode 6 (fortrolig) eller 7 (strengt fortrolig)";
    }
}
