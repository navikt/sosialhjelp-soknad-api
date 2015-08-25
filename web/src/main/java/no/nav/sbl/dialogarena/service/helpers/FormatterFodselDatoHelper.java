package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import no.bekk.bekkopen.person.Fodselsnummer;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static no.bekk.bekkopen.person.FodselsnummerValidator.getFodselsnummer;
import static org.apache.commons.lang3.ArrayUtils.reverse;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;

@Component
public class FormatterFodselDatoHelper extends RegistryAwareHelper {
    public static final String NAVN = "formatterFodelsDato";
    public static final FormatterFodselDatoHelper INSTANS = new FormatterFodselDatoHelper();

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public Helper getHelper() {
        return INSTANS;
    }

    @Override
    public String getBeskrivelse() {
        return "Formatterer en streng til dato på formen dd.mm.aaaa";
    }

    @Override
    public CharSequence apply(Object value, Options options) throws IOException {
        String verdi = value.toString();
        if (verdi.length() == 11) {
            Fodselsnummer fnr = getFodselsnummer(verdi);
            return fnr.getDayInMonth() + "." + fnr.getMonth() + "." + fnr.getBirthYear();
        } else {
            String[] datoSplit = split(verdi, "-");
            reverse(datoSplit);
            return join(datoSplit, ".");
        }
    }
}
