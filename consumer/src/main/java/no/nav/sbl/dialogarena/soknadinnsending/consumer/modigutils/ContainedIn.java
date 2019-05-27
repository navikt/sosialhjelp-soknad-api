package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.io.Serializable;
import java.util.Collection;
import org.apache.commons.collections15.Predicate;

public class ContainedIn<T> implements Predicate<T>, Serializable {
    private final Collection<T> collection;

    public ContainedIn(Iterable<T> elements) {
        this.collection = (Collection)(elements instanceof Collection ? (Collection)elements : IterUtils.on(elements).collect());
    }

    public boolean evaluate(T object) {
        return this.collection.contains(object);
    }
}
