package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;
import static no.nav.sbl.dialogarena.service.HandlebarsUtils.lagItererbarRespons;

@Component
public class FilnavnEttersendteSosialhjelpVedleggHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "filnavnEttersendteSosialhjelpVedlegg";

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);

        List<String> filnavnene = soknad.getVedlegg().stream().map(
                vedlegg -> vedlegg.getFilnavn()).filter(vedlegg -> vedlegg != null && soknad.erEttersending()).collect(Collectors.toList());

        System.out.println(NAVN + getBeskrivelse());

        return lagItererbarRespons(options, filnavnene);
    }

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Helper for å liste ut filnavn for ettersendte vedlegg";
    }
}


