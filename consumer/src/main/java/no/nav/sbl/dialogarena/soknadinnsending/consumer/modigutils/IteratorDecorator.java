package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import java.util.Iterator;

/**
 * Provides a common base for iterators that is used to decorate another iterator with default implementations for
 * {@link #hasNext()} and {@link #remove()}.
 *
 * @param <E>
 *            The type of this iterator
 * @param <D>
 *            The type of the decorated iterator
 */
public abstract class IteratorDecorator<E, D> implements Iterator<E> {

    protected final Iterator<D> decorated;

    protected IteratorDecorator(Iterator<D> decorated) {
        this.decorated = decorated;
    }

    @Override
    public boolean hasNext() {
        return decorated.hasNext();
    }

    @Override
    public void remove() {
        decorated.remove();
    }
}
