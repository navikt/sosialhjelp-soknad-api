package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static no.nav.sbl.sosialhjelp.pdf.HandlebarsUtils.finnWebSoknad;
import static no.nav.sbl.sosialhjelp.pdf.HandlebarsUtils.lagItererbarRespons;

@Component
public class EttersendteVedleggHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "ettersendteVedlegg";

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {

        WebSoknad soknad = finnWebSoknad(options.context);

        List<Vedlegg> alleVedlegg = soknad.getVedlegg();

        Map<String, List<Vedlegg>> vedleggMap = new HashMap<>();

        for (Vedlegg vedlegg : alleVedlegg) {
            if (vedlegg.getInnsendingsvalg().erIkke(LastetOpp)) {
                continue;
            }

            String sammensattNavn = vedlegg.getSkjemaNummer() + "|" + vedlegg.getSkjemanummerTillegg();
            List<Vedlegg> vedleggsGruppe = vedleggMap.get(sammensattNavn);

            if (vedleggsGruppe == null) {
                vedleggsGruppe = new ArrayList<>();
                vedleggMap.put(sammensattNavn, vedleggsGruppe);
            }
            Vedlegg v = new Vedlegg();
            v.setSkjemaNummer(vedlegg.getSkjemaNummer());
            v.setSkjemanummerTillegg(vedlegg.getSkjemanummerTillegg());
            v.setFilnavn(vedlegg.getFilnavn());
            vedleggsGruppe.add(v);
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


