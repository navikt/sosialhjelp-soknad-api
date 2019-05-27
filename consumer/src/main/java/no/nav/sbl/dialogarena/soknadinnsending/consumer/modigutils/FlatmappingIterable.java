package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import static java.util.Collections.emptyIterator;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.commons.collections15.Transformer;

class FlatmappingIterable<IN, OUT> implements Iterable<OUT>, Serializable {

    private final Iterable<IN> elements;
    private final Transformer<? super IN, ? extends Iterable<OUT>> transformer;

    public FlatmappingIterable(Iterable<IN> elements, Transformer<? super IN, ? extends Iterable<OUT>> transformer) {
        this.elements = elements;
        this.transformer = transformer;
    }

    @Override
    public Iterator<OUT> iterator() {
        return new SimpleIterator<OUT>() {
            Iterator<IN> original = elements.iterator();
            Iterator<OUT> currentColl = emptyIterator();
            @Override
            protected Optional<OUT> nextIfAvailable() {
                if (currentColl.hasNext()) {
                    return Optional.optional(currentColl.next());
                } else if (original.hasNext()) {
                    Iterable<OUT> iteratorRef = transformer.transform(original.next());
                    if (iteratorRef != null) {
                        currentColl = iteratorRef.iterator();
                        return Optional.optional(currentColl.next());
                    } else {
                        currentColl = emptyIterator();
                        return nextIfAvailable();
                    }
                } else {
                    return Optional.none();
                }
            }
        };
    }

}

