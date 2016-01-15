package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.NO_LOCALE;
import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

@Component
public class SendtInnInfoHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "sendtInnInfo";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Tilgjengeliggjør informasjon om søknaden (innsendte vedlegg, påkrevde vedlegg og dato)";
    }

    @Override
    public CharSequence apply(Object o, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);
        Map<String, String> infoMap = new HashMap<>();

        DateTimeFormatter dt = DateTimeFormat.forPattern("d. MMMM yyyy', klokken' HH.mm").withLocale(NO_LOCALE);

        infoMap.put("sendtInn", String.valueOf(soknad.getInnsendteVedlegg().size()));
        infoMap.put("ikkeSendtInn", String.valueOf(soknad.hentPaakrevdeVedlegg().size()));
        infoMap.put("innsendtDato", dt.print(DateTime.now()));

        return options.fn(infoMap);
    }
}
