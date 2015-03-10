package no.nav.sbl.dialogarena.common;

import org.apache.commons.collections15.Transformer;

import java.util.Comparator;

/**
 * Klasse med Comparators
 */
public final class Comparators {

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
            return transformer.transform(o1).compareTo(transformer.transform(o2));
        }

    }

}
