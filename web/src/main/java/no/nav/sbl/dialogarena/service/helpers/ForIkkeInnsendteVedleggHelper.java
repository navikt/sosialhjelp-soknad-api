package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;
import static no.nav.sbl.dialogarena.service.HandlebarsUtils.lagItererbarRespons;

@Component
public class ForIkkeInnsendteVedleggHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "forIkkeInnsendteVedlegg";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Itererer over vedlegg som ikke er sendt inn";
    }

    @Override
    public CharSequence apply(Object o, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        List<Vedlegg> vedlegg = soknad.getIkkeInnsendteVedlegg();
        if (vedlegg.isEmpty()) {
            return options.inverse(this);
        } else {
            return lagItererbarRespons(options, vedlegg);
        }
    }
}
