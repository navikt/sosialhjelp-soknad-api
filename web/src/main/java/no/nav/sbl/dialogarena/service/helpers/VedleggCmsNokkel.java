package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class VedleggCmsNokkel extends RegistryAwareHelper<Vedlegg> {


    @Override
    public String getNavn() {
        return "vedleggCmsNokkel";
    }

    @Override
    public String getBeskrivelse() {
        return "Henter teksten for et vedlegg";
    }

    @Override
    public CharSequence apply(Vedlegg vedlegg, Options options) throws IOException {
        return tekstNokkel(vedlegg);
    }
    public String tekstNokkel(Vedlegg vedlegg) {
        String cmsKey = "Dagpenger.vedlegg." + vedlegg.getSkjemaNummer();
        if (!StringUtils.isEmpty(vedlegg.getSkjemanummerTillegg())) {
            cmsKey += "." + vedlegg.getSkjemanummerTillegg();
        }
        return cmsKey + ".bekrefte";
    }

}
