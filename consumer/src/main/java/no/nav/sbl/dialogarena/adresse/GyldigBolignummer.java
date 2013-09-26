package no.nav.sbl.dialogarena.adresse;

import no.nav.modig.lang.collections.SerializablePredicate;
import org.apache.commons.collections15.Predicate;

import java.io.Serializable;

public final class GyldigBolignummer implements Serializable {

    public static final Predicate<String> LHUK_OG_4_SIFFER = new SerializablePredicate<String>() {
        @Override
        public boolean evaluate(String bolignummer) {
            return bolignummer.matches("^[lhukLHUK]\\d{4}$");
        }
    };

    private GyldigBolignummer() { }

}
