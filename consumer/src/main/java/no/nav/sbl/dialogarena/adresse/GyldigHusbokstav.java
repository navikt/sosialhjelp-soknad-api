package no.nav.sbl.dialogarena.adresse;

import no.nav.modig.lang.collections.SerializablePredicate;
import org.apache.commons.collections15.Predicate;

import java.io.Serializable;

public final class GyldigHusbokstav implements Serializable {

    public static final Predicate<String> BOKSTAV = new SerializablePredicate<String>() {
        @Override
        public boolean evaluate(String tegn) {
            return tegn.matches("\\p{L}");
        }
    };

    private GyldigHusbokstav() { }
}
