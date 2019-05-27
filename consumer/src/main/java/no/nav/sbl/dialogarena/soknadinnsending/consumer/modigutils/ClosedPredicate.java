package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import org.apache.commons.collections15.Predicate;

public final class ClosedPredicate<T> implements SerializableFactory<Boolean> {
    private final T parameter;
    private final Predicate<? super T> predicate;

    public ClosedPredicate(T parameter, Predicate<? super T> predicate) {
        this.parameter = parameter;
        this.predicate = predicate;
    }

    public Boolean create() {
        return this.predicate.evaluate(this.parameter);
    }
}
