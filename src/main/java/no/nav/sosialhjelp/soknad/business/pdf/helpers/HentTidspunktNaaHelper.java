package no.nav.sosialhjelp.soknad.business.pdf.helpers;


import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static no.nav.sosialhjelp.soknad.business.pdf.HandlebarContext.SPRAK;

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
        
        ZonedDateTime now = ZonedDateTime.now();

        DateTimeFormatter datoFormatter = DateTimeFormatter.ofPattern("d. MMMM yyyy", SPRAK);
        DateTimeFormatter klokkeslettFormatter = DateTimeFormatter.ofPattern("HH:mm", SPRAK);

        return now.format(datoFormatter) + " " + now.format(klokkeslettFormatter);
    }
}
