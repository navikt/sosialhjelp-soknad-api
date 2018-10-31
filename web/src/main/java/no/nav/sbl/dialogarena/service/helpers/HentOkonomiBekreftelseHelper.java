package no.nav.sbl.dialogarena.service.helpers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Options;

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;

@Component
public class HentOkonomiBekreftelseHelper extends RegistryAwareHelper<String> {

    public static final String NAVN = "hentOkonomiBekreftelse";

    @Override
    public CharSequence apply(final String type, final Options options) throws IOException {
        @SuppressWarnings("unchecked")
        final List<JsonOkonomibekreftelse> bekreftelser = (List<JsonOkonomibekreftelse>) options.context.get("bekreftelse");
        final Optional<JsonOkonomibekreftelse> bekreftelse = bekreftelser.stream()
                .filter(b -> b.getType().equals(type))
                .findFirst();
        
        if (bekreftelse.isPresent()) {
            Context.Builder contextMedVariabel = Context.newBuilder(options.context, options.context.model())
                    .combine("verdi", bekreftelse.get().getVerdi());
            return options.fn(contextMedVariabel.build());
        } else {
            return options.inverse(this);
        }
    }

    @Override
    public String getNavn() { return NAVN; }

    @Override
    public String getBeskrivelse() {
        return "Lager en variabel med en bestemt verdi som kun er tilgjengelig innenfor helperen";
    }
}
