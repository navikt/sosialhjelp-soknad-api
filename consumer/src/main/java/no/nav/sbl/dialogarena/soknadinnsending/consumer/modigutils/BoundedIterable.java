package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import java.io.Serializable;
import java.util.Iterator;

public class BoundedIterable<T> implements Iterable<T>, Serializable {

    private final Iterable<T> iterable;
    private final int maxAmount;

    public BoundedIterable(Iterable<T> iterable, int maxAmount) {
        this.iterable = iterable;
        this.maxAmount = maxAmount;
    }

    @Override
    public Iterator<T> iterator() {
        return new IteratorDecorator<T, T>(iterable.iterator()) {
            int taken = 0;

            @Override
            public boolean hasNext() {
                return taken < maxAmount && super.hasNext();
            }

            @Override
            public T next() {
                taken++;
                return decorated.next();
            }
        };
    }

}
