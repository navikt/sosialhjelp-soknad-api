package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;

import static no.nav.sbl.sosialhjelp.pdf.HandlebarsUtils.finnWebSoknad;

@Component
public class HentEttersendelsestidspunktHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "hentEttersendelsestidspunkt";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Henter tidspunkt for ettersendte vedlegg";
    }

    @Override
    public CharSequence apply(Object o, Options options) throws IOException {

        WebSoknad soknad = finnWebSoknad(options.context);

        if (soknad.erEttersending()) {

            Locale sprak = soknad.getSprak();
            DateTime now = DateTime.now();

            DateTimeFormatter datoFormatter = DateTimeFormat.forPattern("d. MMMM yyyy").withLocale(sprak);
            DateTimeFormatter klokkeslettFormatter = DateTimeFormat.forPattern("HH.mm").withLocale(sprak);

            return datoFormatter.print(now) + " " + klokkeslettFormatter.print(now);
        }
        return "";
    }
}
