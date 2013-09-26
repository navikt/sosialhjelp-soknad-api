package no.nav.sbl.dialogarena.adresse;

import no.nav.modig.lang.collections.SerializablePredicate;
import org.apache.commons.collections15.Predicate;
import org.joda.time.LocalDate;

import static no.nav.modig.lang.collections.PredicateUtils.both;

/**
 * Validator for utløpsdato på midlertidige adresser.
 */
public final class GyldigUtlopsdato implements Predicate<LocalDate> {

    public static LocalDate etAarFremITid() {
        return LocalDate.now().plusYears(1).minusDays(1);
    }

    public static final Predicate<LocalDate> IDAG_ELLER_SENERE = new SerializablePredicate<LocalDate>() {
        @Override
        public boolean evaluate(LocalDate date) {
            return !date.isBefore(LocalDate.now());
        }
    };

    public static final Predicate<LocalDate> MAKS_ETT_AAR_FREM_I_TID = new SerializablePredicate<LocalDate>() {
        @Override
        public boolean evaluate(LocalDate date) {
            return !date.isAfter(etAarFremITid());
        }
    };

    @Override
    public boolean evaluate(LocalDate dato) {
        return both(IDAG_ELLER_SENERE).and(MAKS_ETT_AAR_FREM_I_TID).evaluate(dato);
    }
}
