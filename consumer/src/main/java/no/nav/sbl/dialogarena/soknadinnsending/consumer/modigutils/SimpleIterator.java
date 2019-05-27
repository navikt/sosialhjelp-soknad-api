package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils.Optional.none;

public abstract class SimpleIterator<T> implements Iterator<T> {

    private Optional<? extends T> next = none();

    /**
     * @return The next element if any, or {@link Optional#none() none}.
     */
    protected abstract Optional<? extends T> nextIfAvailable();

    @Override
    public boolean hasNext() {
        next = nextIfAvailable();
        return next.isSome();
    }

    @Override
    public T next() {
        if (!next.isSome() && !hasNext()) {
            throw new NoSuchElementException();
        }
        T toReturn = next.get();
        next = none();
        return toReturn;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}