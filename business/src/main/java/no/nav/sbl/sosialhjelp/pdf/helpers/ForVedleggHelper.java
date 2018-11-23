package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static no.nav.sbl.sosialhjelp.pdf.HandlebarsUtils.finnWebSoknad;
import static no.nav.sbl.sosialhjelp.pdf.HandlebarsUtils.lagItererbarRespons;

@Component
public class ForVedleggHelper extends RegistryAwareHelper<Object> {

    @Override
    public String getNavn() {
        return "forVedlegg";
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
