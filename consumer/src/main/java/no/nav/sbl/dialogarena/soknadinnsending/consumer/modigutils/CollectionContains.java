package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.util.Iterator;
import org.apache.commons.collections15.Predicate;

public class CollectionContains<E, I extends Iterable<? extends E>> implements Predicate<I> {
    private final Predicate<? super E> elementPredicate;

    public CollectionContains(Predicate<? super E> elementPredicate) {
        this.elementPredicate = elementPredicate;
    }

    public boolean evaluate(I iterable) {
        Iterator i$ = iterable.iterator();

        Object elem;
        do {
            if (!i$.hasNext()) {
                return false;
            }

            elem = i$.next();
        } while(!this.elementPredicate.evaluate(elem));

        return true;
    }
}
