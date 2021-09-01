package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static no.nav.sosialhjelp.soknad.business.pdf.HandlebarContext.SPRAK;

@Component
public class FormaterDatoHelper extends RegistryAwareHelper<String>{

    @Override
    public String getNavn() {
        return "formaterDato";
    }

    @Override
    public String getBeskrivelse() {
        return "Formaterer en innsendt dato på et gitt format som også sendes inn";
    }

    @Override
    public CharSequence apply(String datoStreng, Options options) {
        if (datoStreng == null) {
            return "";
        }

        final String format = options.param(0);
        final LocalDate date = LocalDate.parse(datoStreng);
        return date.format(DateTimeFormatter.ofPattern(format, SPRAK));
    }
}
