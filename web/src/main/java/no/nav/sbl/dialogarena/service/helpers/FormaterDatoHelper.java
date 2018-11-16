package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class FormaterDatoHelper extends RegistryAwareHelper<Object>{

    public static final String NAVN = "formaterDato";

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {
        if (key != null) {
            try {
                LocalDate datetime = LocalDate.parse(key.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return datetime.format(DateTimeFormatter.ofPattern("ddMMyyyy")).toString();
            }
            catch (java.time.format.DateTimeParseException e) {
                return "";
            }
        }
        else {
            return "";
        }
    }

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Returnerer personnr fra personIdentifikator";
    }
}
