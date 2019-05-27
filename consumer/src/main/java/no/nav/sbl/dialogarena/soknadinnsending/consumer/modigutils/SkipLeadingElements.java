package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import java.util.Iterator;

class SkipLeadingElements<T> implements Iterable<T> {

    private final int skipAmount;
    private final Iterable<T> elements;

    SkipLeadingElements(int amount, Iterable<T> elements) {
        this.skipAmount = amount;
        this.elements = elements;
    }

    @Override
    public Iterator<T> iterator() {
        return new ReadOnlyIterator<T>() {
            final Iterator<T> iterator = elements.iterator();
            boolean skipped = false;

            @Override
            public boolean hasNext() {
                if (skipped) {
                    return iterator.hasNext();
                }
                for (int i = 0; i < skipAmount; i++) {
                    if (!iterator.hasNext()) {
                        return false;
                    }
                    iterator.next();
                }
                skipped = true;
                return iterator.hasNext();
            }

            @Override
            public T next() {
                if (!skipped) {
                    hasNext();
                }
                return iterator.next();
            }
        };
    }

}
