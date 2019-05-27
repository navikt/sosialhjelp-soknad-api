package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import java.util.Iterator;

public class IndexingIterable<E> extends CollectableIterable<Elem<E>> {
    private final Iterable<E> iterable;
    private final int startIndex;

    public IndexingIterable(int startIndex, Iterable<E> iterable) {
        this.startIndex = startIndex;
        this.iterable = iterable;
    }

    @Override
    public Iterator<Elem<E>> iterator() {
        return new IteratorDecorator<Elem<E>, E>(iterable.iterator()) {
            private int index = startIndex;

            @Override
            public Elem<E> next() {
                return Elem.elem(index++, decorated.next());
            }
        };
    }
}