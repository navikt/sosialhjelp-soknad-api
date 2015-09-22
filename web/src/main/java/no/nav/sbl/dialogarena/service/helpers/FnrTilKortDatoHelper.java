package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.bekk.bekkopen.person.Fodselsnummer;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static no.bekk.bekkopen.person.FodselsnummerValidator.getFodselsnummer;

@Component
public class FnrTilKortDatoHelper extends RegistryAwareHelper<Object> {
    public static final String NAVN = "fnrTilKortDato";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Formatterer et gyldig fødselnummer til dato på formatet dd.mm.aaaa";
    }

    @Override
    public CharSequence apply(Object value, Options options) throws IOException {
        Fodselsnummer fnr = getFodselsnummer(value.toString());
        return fnr.getDayInMonth() + "." + fnr.getMonth() + "." + fnr.getBirthYear();
    }
}
