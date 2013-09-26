package no.nav.sbl.dialogarena.kontaktdetaljer;

import no.nav.modig.lang.collections.SerializablePredicate;
import org.apache.commons.collections15.Predicate;

import java.io.Serializable;

import static no.nav.modig.lang.collections.PredicateUtils.both;
import static no.nav.modig.lang.collections.PredicateUtils.equalOrLessThan;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.modig.lang.collections.TransformerUtils.lengthOfString;


public class GyldigEpostadresse implements Predicate<String>, Serializable {

    public static final int MAKS_ANTALL_TEGN = 50;

    public static final Predicate<String> IKKE_MER_ENN_MAKS_ANTALL_TEGN = where(lengthOfString(), equalOrLessThan(MAKS_ANTALL_TEGN));

    public static final Predicate<String> HAR_ALFAKROLL_INNI_ADRESSEN = new SerializablePredicate<String>() {
        @Override
        public boolean evaluate(String epostadresse) {
            int aringPosition = epostadresse.indexOf('@');
            return aringPosition > 0 && aringPosition < epostadresse.length() - 1;
        }
    };

    @Override
    public boolean evaluate(String epostadresse) {
        return both(IKKE_MER_ENN_MAKS_ANTALL_TEGN).and(HAR_ALFAKROLL_INNI_ADRESSEN).evaluate(epostadresse);
    }
}
