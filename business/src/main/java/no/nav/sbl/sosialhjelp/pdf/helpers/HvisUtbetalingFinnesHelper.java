package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class HvisUtbetalingFinnesHelper extends RegistryAwareHelper<String> {

    public static final String NAVN = "hvisUtbetalingFinnes";

    @Override
    public CharSequence apply(String type, Options options) throws IOException {
        @SuppressWarnings("unchecked") List<JsonOkonomiOpplysningUtbetaling> bekreftelser = (List<JsonOkonomiOpplysningUtbetaling>) options.context.get("utbetaling");
        Optional<JsonOkonomiOpplysningUtbetaling> bekreftelse = bekreftelser.stream()
                .filter(b -> b.getType().equals(type))
                .findFirst();
        
        if (bekreftelse.isPresent()) {
            return options.fn(this);
        } else {
            return options.inverse(this);
        }
    }

    @Override
    public String getNavn() { return NAVN; }

    @Override
    public String getBeskrivelse() {
        return "Sjekker om det finnes utbetalinger av en gitt type";
    }
}
