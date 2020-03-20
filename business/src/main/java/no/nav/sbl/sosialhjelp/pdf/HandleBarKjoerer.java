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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HandleBarKjoerer implements HtmlGenerator, HandlebarRegistry {

    private Map<String, Helper> helpers = new HashMap<>();

    @Override
    public String fyllHtmlMalMedInnhold(JsonInternalSoknad jsonInternalSoknad, boolean utvidetSoknad) throws IOException {
        final HandlebarContext context = new HandlebarContext(jsonInternalSoknad, utvidetSoknad, false, "");
        String renderedHtml = getHandlebars()
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
        return stripNonValidXMLCharacters(renderedHtml);
    }

    @Override
    public String fyllHtmlMalMedInnhold(JsonInternalSoknad internalSoknad, String file, boolean erEttersending, String eier) throws IOException {
        final HandlebarContext context = new HandlebarContext(internalSoknad, false, erEttersending, eier);

        String renderedHtml = getHandlebars()
                .compile(file)
                .apply(Context.newBuilder(context).build());
        return stripNonValidXMLCharacters(renderedHtml);
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

    private String stripNonValidXMLCharacters(String in) {
        if (in == null || in.isEmpty()) return "";

        StringBuilder out = new StringBuilder();
        char character;
        for (int i = 0; i < in.length(); i++) {
            character = in.charAt(i);
            if (
                    character == 0x9 ||
                    character == 0xA ||
                    character == 0xD ||
                    character >= 0x20 && character <= 0xD7FF ||
                    character >= 0xE000 && character <= 0xFFFD)
                out.append(character);
        }
        return out.toString();
    }
}