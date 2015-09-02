package no.nav.sbl.dialogarena.service;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class Hjelpemetoder {
    public static <T> String lagItererbarRespons(Options options, List<T> liste) throws IOException {
        Context parent = options.context;
        StringBuilder stringBuilder = new StringBuilder();
        int index = 0;
        Iterator<T> iterator = liste.iterator();
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
}