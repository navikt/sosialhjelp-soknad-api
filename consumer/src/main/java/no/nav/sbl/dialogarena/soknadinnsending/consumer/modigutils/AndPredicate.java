package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.PredicateUtils;

import java.io.Serializable;

/**
 * Egen implementasjon av AndPredicate. Støtter videre chaining av predikater
 */
public class AndPredicate<T> implements org.apache.commons.collections15.Predicate<T>, Serializable {
    private final Predicate<? super T> predicate1;
    private final Predicate<? super T> predicate2;

    public AndPredicate(Predicate<? super T> predicate) {
        this(PredicateUtils.<T> truePredicate(), predicate);
    }

    public AndPredicate(Predicate<? super T> predicate1, Predicate<? super T> predicate2) {
        this.predicate2 = predicate2;
        this.predicate1 = predicate1;
    }

    @Override
    public boolean evaluate(T t) {
        return predicate1.evaluate(t) && predicate2.evaluate(t);
    }

    public AndPredicate<T> and(Predicate<? super T> predicate3) {
        return new AndPredicate<T>(this, predicate3);
    }
}
