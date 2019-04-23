package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

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
    public CharSequence apply(String datoStreng, Options options) throws IOException {
        if (datoStreng == null) {
            return "";
        }

        String format = options.param(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        DateTimeFormatter parser = DateTimeFormatter.ofPattern("");
        throw new RuntimeException();
        //return formatter.format(java.time.LocalDate.now());
    }
}
