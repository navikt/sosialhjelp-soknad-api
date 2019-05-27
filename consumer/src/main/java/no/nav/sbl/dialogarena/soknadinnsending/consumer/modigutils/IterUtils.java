package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import static java.util.Arrays.asList;

import java.util.Comparator;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.NOPTransformer;

public final class IterUtils {

    /**
     * Decorates an arbitrary to make it subject to various operations, such as:
     *
     * <ul>
     * <li>{@link PreparedIterable#filter(org.apache.commons.collections15.Predicate) filter}</li>
     * <li>{@link PreparedIterable#map(org.apache.commons.collections15.Transformer) map}</li>
     * <li>{@link PreparedIterable#take(int) take}</li>
     * <li>{@link PreparedIterable#indexed() indexed}</li>
     * </ul>
     *
     * @param iterable
     *            The iterable to decorate
     * @return the {@link PreparedIterable}
     */
    public static <T> PreparedIterable<T> on(Iterable<T> iterable) {
        return new PreparedIterable<T>(iterable);
    }

    /**
     * Make a view of a map as an Iterable of its {@link java.util.Map.Entry} entries.
     *
     * @param map
     *            The map
     * @return The Iterable view of the map.
     *
     * @see TransformerUtils#key()
     * @see TransformerUtils#value()
     */
    public static <K, V> PreparedIterable<Map.Entry<K, V>> on(Map<K, V> map) {
        return new PreparedIterable<Map.Entry<K, V>>(new MapEntries<K, V>(map));
    }

    /**
     * Convenience method to accept an array which will be decorated as a {@link PreparedIterable}.
     *
     * @see #on(Iterable)
     * @param elements
     *            The array to decorate
     * @return the {@link PreparedIterable}
     */
    public static <T> PreparedIterable<T> on(T[] elements) {
        return on(elements != null ? asList(elements) : null);
    }


    /**
     * {@link Comparator} which compares using the natural ordering of any {@link Comparable}.
     * This can be used if you have a type with a natural ordering, but need an external
     * {@link Comparator} with the same behavior.
     */
    public static <T extends Comparable<T>> EnhancedComparator<T> byNaturalOrderingOf(Class<T> type) {
        return by(NOPTransformer.<T>getInstance());
    }

    /**
     * Create a {@link Comparator} which compares the result from applying a transformer on each of
     * the objects to compare. This is useful for externalizing the order of arbitrary objects
     * (i.e. instead of implementing {@link Comparable} on objects which do not have a natural ordering)
     * and derive the order based on an attribute of the comparing objects.
     *
     * @param yieldsComparable A transformer which yields some {@link Comparable} object to use for
     *                         comparing.
     *
     * @return The {@link EnhancedComparator Comparator}.
     */
    public static <T, C extends Comparable<? super C>> EnhancedComparator<T> by(Transformer<T, C> yieldsComparable) {
        return new IncrementingByTransformer<T, C>(yieldsComparable);
    }

    private IterUtils() {
    }

    static {
        new IterUtils();
    }

}
