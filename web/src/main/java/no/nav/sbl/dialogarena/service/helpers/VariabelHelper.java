package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.io.IOException;

public class VariabelHelper implements Helper<String> {

    public static final Helper<String> INSTANCE = new VariabelHelper();

    public static final String NAME = "variabel";

    @Override
    public CharSequence apply(final String variableName, final Options options) throws IOException {
        Context.Builder contextMedVariabel = Context.newBuilder(options.context, options.context.model())
                .combine(variableName, options.param(0));
        return options.fn(contextMedVariabel.build());
    }
}
