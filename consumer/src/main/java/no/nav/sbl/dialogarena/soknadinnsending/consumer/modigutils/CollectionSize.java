package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.io.Serializable;
import java.util.Collection;
import org.apache.commons.collections15.Predicate;

public class CollectionSize<T extends Collection<?>> implements Predicate<T>, Serializable {
    private final Predicate<Integer> sizePredicate;

    public CollectionSize(Predicate<Integer> sizePredicate) {
        this.sizePredicate = sizePredicate;
    }

    public boolean evaluate(T collection) {
        return this.sizePredicate.evaluate(collection.size());
    }
}
