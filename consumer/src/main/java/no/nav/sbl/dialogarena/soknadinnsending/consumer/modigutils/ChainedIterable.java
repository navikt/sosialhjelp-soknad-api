package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import org.apache.commons.collections15.iterators.IteratorChain;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

class ChainedIterable<T> implements Iterable<T>, Serializable {

    private final Iterable<? extends T> first;
    private final Iterable<? extends T> second;

    public ChainedIterable(Iterable<? extends T> first, Iterable<? extends T> second) {
        this.first = defaultIfNull(first, Collections.<T> emptyList());
        this.second = defaultIfNull(second, Collections.<T> emptyList());
    }

    @Override
    public Iterator<T> iterator() {
        return new IteratorChain<T>(first.iterator(), second.iterator());
    }
}
