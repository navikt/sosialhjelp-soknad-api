package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;
import static no.nav.sbl.dialogarena.service.HandlebarsUtils.lagItererbarRespons;

@Component
public class ForVedleggHelper extends RegistryAwareHelper<Object> {


    @Override
    public String getNavn() {
        return "forVedlegg";
    }

    @Override
    public Helper<Object> getHelper() {
        return this;
    }

    @Override
    public String getBeskrivelse() {
        return "Lar en iterere over alle påkrevde vedlegg på en søknad";
    }

    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        List<Vedlegg> vedlegg = soknad.hentPaakrevdeVedlegg();
        if (vedlegg.isEmpty()) {
            return options.inverse(this);
        } else {
            return lagItererbarRespons(options, vedlegg);
        }
    }
}
