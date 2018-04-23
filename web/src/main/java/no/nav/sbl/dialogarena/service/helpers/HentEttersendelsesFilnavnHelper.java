package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;

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
        WebSoknad soknad = finnWebSoknad(options.context);

        List<Vedlegg> vedlegg = soknad.getVedlegg();

        System.out.println(this.getClass().getName() + " : " + NAVN);

        String s;
        StringJoiner joiner = new StringJoiner("<br/ >");
        for (Vedlegg vedlegget : vedlegg) {

            System.out.println("Filnavn ettersending : " + vedlegget.getFilnavn());

            if (Boolean.parseBoolean(vedlegget.getFilnavn())) {
                joiner.add(vedlegget.getFilnavn());
            }
        }
        return joiner.toString();

    }
}
