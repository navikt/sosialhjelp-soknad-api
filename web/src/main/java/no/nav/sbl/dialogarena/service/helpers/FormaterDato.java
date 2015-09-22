package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;

@Component
public class FormaterDato extends RegistryAwareHelper<String>{
    private final Locale locale = new Locale("nb", "NO");

    @Override
    public String getNavn() {
        return "formaterDato";
    }

    @Override
    public String getBeskrivelse() {
        return "Formaterer en innsendt dato på et gitt format som også sendes inn";
    }

    @Override
    public CharSequence apply(String datoStreng, Options options) throws IOException {
        LocalDate date = new LocalDate(datoStreng);
        String format = options.param(0);

        return date.toString(format, locale);
    }
}
