package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;
import static no.nav.sbl.dialogarena.service.HandlebarsUtils.lagItererbarRespons;

@Component
public class EttersendteVedleggHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "ettersendteVedlegg";

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {

        System.out.println(NAVN + getBeskrivelse());

        WebSoknad soknad = finnWebSoknad(options.context);

        //    List<String> filnavnene = soknad.getVedlegg().stream().map(
        //          vedlegg -> vedlegg.getFilnavn()).filter(vedlegg -> vedlegg != null).collect(Collectors.toList());

        List<Vedlegg> alleVedlegg = soknad.getVedlegg();

        Map<String, Vedlegg> vedleggMap = new HashMap<>();

        for (Vedlegg vedlegg : alleVedlegg) {
          /*  if (vedlegg.getInnsendingsvalg().erIkke(LastetOpp)) {
                continue;
            }*/

            String sammensattNavn = vedlegg.getSkjemaNummer() + "|" + vedlegg.getSkjemanummerTillegg();
            Vedlegg vedlegget = vedleggMap.get(sammensattNavn);

            if (vedlegget == null) {
                vedlegget = new Vedlegg();
                vedlegget.setSkjemaNummer(vedlegg.getSkjemaNummer());
                vedlegget.setSkjemanummerTillegg(vedlegg.getSkjemanummerTillegg());
                vedlegget.setFilnavn(vedlegg.getFilnavn());
                vedleggMap.put(sammensattNavn, vedlegget);
            }

        }

        return lagItererbarRespons(options, vedleggMap.values());
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


