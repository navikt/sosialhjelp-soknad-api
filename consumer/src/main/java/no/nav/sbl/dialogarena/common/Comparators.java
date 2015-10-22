package no.nav.sbl.dialogarena.common;

import org.apache.commons.collections15.Transformer;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public final class Comparators {
    private static Locale norwegianLocale = new Locale("nb", "NO");

    public static <T, C extends Comparable<C>> Comparator<T> compareBy(Transformer<T, C> transformer) {
        return new TransformerComparator<T, C>(transformer);
    }

    private static class TransformerComparator<T, C extends Comparable<C>> implements Comparator<T> {

        private final Transformer<T, C> transformer;

        public TransformerComparator(Transformer<T, C> transformer) {
            this.transformer = transformer;
        }

        @Override
        public int compare(T o1, T o2) {
            return Collator.getInstance(norwegianLocale).compare(transformer.transform(o1), transformer.transform(o2));
        }
    }
}
