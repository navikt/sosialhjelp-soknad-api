package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import java.util.Iterator;

public abstract class ReadOnlyIterator<T> implements Iterator<T> {

    /**
     * Throws {@link UnsupportedOperationException}.
     */
    @Override
    public final void remove() {
        throw new UnsupportedOperationException(getClass().getName() + " does not support remove() operation");
    };
}
