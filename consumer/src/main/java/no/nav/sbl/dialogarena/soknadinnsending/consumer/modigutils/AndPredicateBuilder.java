package no.nav.sbl.dialogarena.soknadinnsending.consumer.modigutils;

import no.nav.modig.lang.collections.predicate.AndPredicate;
import org.apache.commons.collections15.Predicate;

/**
 * Bygger et AND-predikat, i.e. begge predikatene må evaluere til <code>true</code> for at hele AND-predikatet skal være true.
 *
 * @param <T>
 *            Typen til objektene som sjekkes av predikatet
 */
public class AndPredicateBuilder<T> {
    private final Predicate<T> firstPredicate;

    public AndPredicateBuilder(Predicate<T> firstPredicate) {
        this.firstPredicate = firstPredicate;
    }

    public <S extends T> AndPredicate<S> and(Predicate<? super S> secondPredicate) {
        return new AndPredicate<S>(firstPredicate, secondPredicate);
    }
}
