package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

@Component
public class PersonnrHelper extends RegistryAwareHelper<Object>{

    public static final String NAVN = "personnr";

    @Override
    public CharSequence apply(Object key, Options options) {
        if (key != null && key.toString().length() == 11) {
            return key.toString().substring(6, 11);
        } else {
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
