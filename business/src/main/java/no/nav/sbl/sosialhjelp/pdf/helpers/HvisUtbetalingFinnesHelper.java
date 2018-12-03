package no.nav.sbl.sosialhjelp.pdf.helpers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.github.jknack.handlebars.Options;

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;

@Component
public class HvisUtbetalingFinnesHelper extends RegistryAwareHelper<String> {

    public static final String NAVN = "hvisUtbetalingFinnes";

    @Override
    public CharSequence apply(final String type, final Options options) throws IOException {
        @SuppressWarnings("unchecked")
        final List<JsonOkonomiOpplysningUtbetaling> bekreftelser = (List<JsonOkonomiOpplysningUtbetaling>) options.context.get("utbetaling");
        final Optional<JsonOkonomiOpplysningUtbetaling> bekreftelse = bekreftelser.stream()
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
