package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class HvisOkonomiskVerdiHelper extends RegistryAwareHelper<Object>{

    public static final String NAVN = "hvisOkonomiskVerdi";

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {
        ArrayList<String> okonomiskVerdiNavn = new ArrayList<>();
        okonomiskVerdiNavn.add("bolig");
        okonomiskVerdiNavn.add("campingvogn");
        okonomiskVerdiNavn.add("kjoretoy");
        okonomiskVerdiNavn.add("fritidseiendom");
        okonomiskVerdiNavn.add("annet");
        if (key != null && okonomiskVerdiNavn.contains(key.toString())){
            return options.fn(this);
        } else {
            return options.inverse(this);
        }
    }

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Sjekker om en streng er av typen økonomisk verdi";
    }
}
