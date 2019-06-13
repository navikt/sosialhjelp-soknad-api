package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import static no.nav.sbl.sosialhjelp.pdf.HandlebarContext.SPRAK;

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
        final LocalDate date = new LocalDate(datoStreng);
        
        return date.toString(format, SPRAK);            
    }
}
