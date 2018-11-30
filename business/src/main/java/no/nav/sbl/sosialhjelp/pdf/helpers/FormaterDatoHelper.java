package no.nav.sbl.sosialhjelp.pdf.helpers;

import static no.nav.sbl.sosialhjelp.pdf.HandlebarContext.SPRAK;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import com.github.jknack.handlebars.Options;

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
        try {
            String format = options.param(0);
            if (format.toLowerCase().contains("h")) {
                LocalDateTime date = LocalDateTime.parse(datoStreng, 
                        DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX", SPRAK));
                
                DateTimeFormatter datoFormatter = DateTimeFormatter.ofPattern(format);
                return date.format(datoFormatter);
            }
            else {
                LocalDate date = new LocalDate(datoStreng);
                return date.toString(format, SPRAK);            
            }            
        }
        catch (DateTimeParseException ex) {
            return "";
        }
    }
}
