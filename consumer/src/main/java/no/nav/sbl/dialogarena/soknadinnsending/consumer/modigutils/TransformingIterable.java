package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import org.apache.commons.collections15.Transformer;

import java.util.Iterator;

class TransformingIterable<IN, OUT> extends CollectableIterable<OUT> {

    private final Iterable<IN> elements;
    private final Transformer<? super IN, ? extends OUT> transformer;

    public TransformingIterable(Iterable<IN> elements, Transformer<? super IN, ? extends OUT> transformer) {
        this.elements = elements;
        this.transformer = transformer;
    }

    @Override
    public Iterator<OUT> iterator() {
        return new IteratorDecorator<OUT, IN>(elements.iterator()) {

            @Override
            public OUT next() {
                return transformer.transform(decorated.next());
            }
        };
    }

}
