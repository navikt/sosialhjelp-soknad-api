package no.nav.sbl.dialogarena.adresse;

import no.nav.modig.lang.collections.SerializablePredicate;
import org.apache.commons.collections15.Predicate;

import static no.nav.modig.lang.collections.PredicateUtils.blank;
import static no.nav.modig.lang.collections.PredicateUtils.either;
import static no.nav.modig.lang.collections.PredicateUtils.numeric;
import static no.nav.modig.lang.collections.PredicateUtils.withLength;


public final class GyldigPostnummer {

    public static final Predicate<String> FIRE_TEGN = either(blank()).or(withLength(4));

    public static final Predicate<String> NUMERISK = either(blank()).or(numeric());

    public static Predicate<String> finnesI(final Adressekodeverk kodeverk) {
        return either(blank()).or(new SerializablePredicate<String>() {
            @Override
            public boolean evaluate(String postnummer) {
                return kodeverk.getPoststed(postnummer) != null;
            }
        });
    }

}
