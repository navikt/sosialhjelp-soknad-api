package no.nav.sbl.sosialhjelp.pdf.helpers;


import com.github.jknack.handlebars.Options;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

import static no.nav.sbl.sosialhjelp.pdf.HandlebarContext.SPRAK;

@Component
public class HentTidspunktNaaHelper extends RegistryAwareHelper<Object> {

    public static final String NAVN = "hentTidspunktNaa";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Henter dato og klokkeslett akkurat nå";
    }

    @Override
    public CharSequence apply(Object o, Options options) {
        
        DateTime now = DateTime.now();

        DateTimeFormatter datoFormatter = DateTimeFormat.forPattern("d. MMMM yyyy").withLocale(SPRAK);
        DateTimeFormatter klokkeslettFormatter = DateTimeFormat.forPattern("HH:mm").withLocale(SPRAK);

        return datoFormatter.print(now) + " " + klokkeslettFormatter.print(now);
    }
}
