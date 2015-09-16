package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UkedagHelper extends RegistryAwareHelper<String>{

    private String[] dager = {"Mandag", "Tirsdag", "Onsdag", "Torsdag", "Fredag", "Lørdag", "Søndag"};

    @Override
    public String getNavn() {
        return "ukedag";
    }

    @Override
    public Helper<String> getHelper() {
        return this;
    }

    @Override
    public String getBeskrivelse() {
        return "Returnerer ukedagen for en dato";
    }

    @Override
    public CharSequence apply(String datoStreng, Options options) throws IOException {
        LocalDate date = new LocalDate(datoStreng);
        return dager[date.getDayOfWeek() - 1];
    }
}
