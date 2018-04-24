package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

@Component
public class HentEttersendelsesFilnavnHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "hentEttersendelsesFilnavn";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Henter listen over filnavn for ettersendte vedlegg til bruk i kvittering";
    }

    @Override
    public CharSequence apply(Object o, Options options) throws IOException {
        final WebSoknad soknad = finnWebSoknad(options.context);

        return soknad.getVedlegg().stream().map(
                vedlegg -> vedlegg.getFilnavn()).filter(vedlegg -> vedlegg != null).collect(Collectors.joining("<br />"));

    }
}
