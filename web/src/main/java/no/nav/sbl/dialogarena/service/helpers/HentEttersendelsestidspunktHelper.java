package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

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

            DateTime dateTime = soknad.getOpprettetDato();

            return new Date(dateTime.getMillis()).toLocaleString();

        }
        return "";
    }
}
