package no.nav.sbl.sosialhjelp.pdf;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.cache.ConcurrentMapTemplateCache;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HandleBarKjoerer implements HtmlGenerator, HandlebarRegistry {

    private Map<String, Helper> helpers = new HashMap<>();

    public String fyllHtmlMalMedInnhold(JsonInternalSoknad jsonInternalSoknad, JsonAdresse midlertidigAdresse) throws IOException {
        return fyllHtmlMalMedInnhold(jsonInternalSoknad, midlertidigAdresse, false);
    }

    @Override
    public String fyllHtmlMalMedInnhold(JsonInternalSoknad jsonInternalSoknad, JsonAdresse midlertidigAdresse, boolean utvidetSoknad) throws IOException {
        final HandlebarContext context = new HandlebarContext(jsonInternalSoknad, midlertidigAdresse, utvidetSoknad, false, "");
        return getHandlebars()
                .infiniteLoops(true)
                .compile("/skjema/soknad")
                .apply(Context.newBuilder(context)
                        .resolver(
                                JavaBeanValueResolver.INSTANCE,
                                FieldValueResolver.INSTANCE,
                                MapValueResolver.INSTANCE,
                                MethodValueResolver.INSTANCE
                        )
                        .build());
    }

    @Override
    public String fyllHtmlMalMedInnhold(JsonInternalSoknad internalSoknad, String file, boolean erEttersending, String eier) throws IOException {
        final HandlebarContext context = new HandlebarContext(internalSoknad, null, false, erEttersending, eier);

        return getHandlebars()
                .compile(file)
                .apply(Context.newBuilder(context).build());
    }

    @Override
    public void registrerHelper(String name, Helper helper) {
        helpers.put(name, helper);
    }

    private Handlebars getHandlebars() {
        Handlebars handlebars = new Handlebars()
                .with(new ConcurrentMapTemplateCache());

        for (Map.Entry<String, Helper> helper : helpers.entrySet()) {
            handlebars.registerHelper(helper.getKey(), helper.getValue());
        }

        return handlebars;
    }

}