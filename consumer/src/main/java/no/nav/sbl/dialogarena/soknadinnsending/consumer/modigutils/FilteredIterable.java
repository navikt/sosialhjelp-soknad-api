package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import org.apache.commons.collections15.Predicate;

import java.io.Serializable;
import java.util.Iterator;

import static org.apache.commons.collections15.IteratorUtils.filteredIterator;

final class FilteredIterable<T> implements Iterable<T>, Serializable {
    private final Predicate<? super T> predicate;
    private final Iterable<T> iterable;

    FilteredIterable(Iterable<T> iterable, Predicate<? super T> predicate) {
        this.iterable = iterable;
        this.predicate = predicate;
    }

    @Override
    public Iterator<T> iterator() {
        return filteredIterator(iterable.iterator(), predicate);
    }
}