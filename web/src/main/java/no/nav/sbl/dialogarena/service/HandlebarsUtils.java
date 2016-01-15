package no.nav.sbl.dialogarena.service;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.service.oppsummering.OppsummeringsContext;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

public class HandlebarsUtils {

    public static final Locale NO_LOCALE = new Locale("nb", "no");


    public static <T> String lagItererbarRespons(Options options, Iterable<T> iterable) throws IOException {
        Context parent = options.context;
        StringBuilder stringBuilder = new StringBuilder();
        int index = 0;
        Iterator<T> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            boolean first = index == 0;
            boolean even = index % 2 == 0;
            boolean last = !iterator.hasNext();
            Context current = Context.newContext(parent, element)
                    .data("index", index)
                    .data("first", first ? "first" : "")
                    .data("last", last ? "last" : "")
                    .data("odd", even ? "" : "odd")
                    .data("even", even ? "even" : "");
            stringBuilder.append(options.fn(current));
            index++;
        }
        return stringBuilder.toString();
    }


    public static WebSoknad finnWebSoknad(Context context) {
        if (context == null) {
            return null;
        } else if (context.model() instanceof WebSoknad) {
            return (WebSoknad) context.model();
        } else if (context.model() instanceof OppsummeringsContext) {
            return ((OppsummeringsContext) context.model()).soknad;
        } else {
            return finnWebSoknad(context.parent());
        }
    }

    public static Faktum finnFaktum(Context context) {
        if (context == null) {
            return null;
        } else if (context.model() instanceof Faktum) {
            return (Faktum) context.model();
        } else {
            return finnFaktum(context.parent());
        }
    }
}