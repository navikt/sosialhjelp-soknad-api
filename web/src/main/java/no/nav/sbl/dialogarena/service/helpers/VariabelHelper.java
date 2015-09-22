package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Options;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class VariabelHelper extends RegistryAwareHelper<String> {

    public static final String NAVN = "variabel";

    @Override
    public CharSequence apply(final String variableName, final Options options) throws IOException {
        Context.Builder contextMedVariabel = Context.newBuilder(options.context, options.context.model())
                .combine(variableName, options.param(0));
        return options.fn(contextMedVariabel.build());
    }

    @Override
    public String getNavn() { return NAVN; }

    @Override
    public String getBeskrivelse() {
        return "Lager en variabel med en bestemt verdi som kun er tilgjengelig innenfor helperen";
    }
}
