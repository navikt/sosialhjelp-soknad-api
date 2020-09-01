package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Options;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class ManglerSamtykkeEllerSantHelper extends RegistryAwareHelper<String> {

    public static final String NAVN = "manglerSamtykkeEllerSant";

    @Override
    public CharSequence apply(final String type, final Options options) throws IOException {
        final Boolean otherBoolean = options.param(0);
        if(otherBoolean) return createReturnVariable(options, true);

        if (options.context.get("bekreftelse") == null) {
            return options.fn(this);
        }
        @SuppressWarnings("unchecked")
        final List<JsonOkonomibekreftelse> bekreftelser = (List<JsonOkonomibekreftelse>) options.context.get("bekreftelse");
        final Optional<JsonOkonomibekreftelse> bekreftelse = bekreftelser.stream()
                .filter(b -> b.getType().equals(type))
                .findFirst();
        
        if (bekreftelse.isPresent()) {
            Boolean verdi = bekreftelse.get().getVerdi();
            if(!verdi)
                return options.fn(this);
            else
                return options.inverse(this);
        }
        return options.fn(this);
    }

    private CharSequence createReturnVariable(Options options, Boolean verdi) throws IOException {
        Context contextVerdi = Context.newBuilder(options.context, options.context.model())
                .combine("verdi", verdi).build();
        return options.fn(contextVerdi);
    }

    @Override
    public String getNavn() { return NAVN; }

    @Override
    public String getBeskrivelse() {
        return "Sjekker om vi har samtykke eller at other boolean er sann!";
    }
}
