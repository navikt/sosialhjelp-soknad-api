package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import java.util.Comparator;

/**
 * An extension of {@link Comparator} which offers several decorators which
 * modifies the original Comparator.
 *
 * @param <T> the type of objects that may be compared by this comparator
 */
public interface EnhancedComparator<T> extends Comparator<T> {

    /**
     * @return a new Comparator which reverses the order.
     */
    EnhancedComparator<T> descending();

    /**
     * @return a new null-safe Comparator which treats <code>null</code> as the lesser value.
     */
    EnhancedComparator<T> nullComesFirst();

    /**
     * @return a new null-safe Comparator which treats <code>null</code> as the greater value.
     */
    EnhancedComparator<T> nullComesLast();

}
