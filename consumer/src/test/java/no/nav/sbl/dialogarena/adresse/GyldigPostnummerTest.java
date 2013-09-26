package no.nav.sbl.dialogarena.adresse;

import org.apache.commons.collections15.Predicate;
import org.junit.Test;

import java.io.Serializable;

import static no.nav.modig.lang.collections.PredicateUtils.both;
import static no.nav.sbl.dialogarena.adresse.GyldigPostnummer.FIRE_TEGN;
import static no.nav.sbl.dialogarena.adresse.GyldigPostnummer.NUMERISK;
import static no.nav.sbl.dialogarena.adresse.GyldigPostnummer.finnesI;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GyldigPostnummerTest implements Adressekodeverk, Serializable {

    private static final String IKKE_EKSISTERENDE_POSTNUMMER = "1111";

    private Predicate<String> gyldigPostnummer = both(FIRE_TEGN).and(NUMERISK).and(finnesI(this));

    @Test
    public void postnummerHarAkkurat4Siffer() {
        assertTrue(gyldigPostnummer.evaluate("1234"));

        assertFalse(gyldigPostnummer.evaluate("123"));
        assertFalse(gyldigPostnummer.evaluate("12345"));
    }

    @Test
    public void postnummerHarIkkeAndreTegnEnnSiffer() {
        assertFalse(gyldigPostnummer.evaluate("123A"));
    }

    @Test
    public void blankePostnummerGodtas() {
        assertTrue(gyldigPostnummer.evaluate(""));
        assertTrue(gyldigPostnummer.evaluate(null));
    }

    @Test
    public void godtarKunGyldigePostnummer() {
        assertFalse(gyldigPostnummer.evaluate(IKKE_EKSISTERENDE_POSTNUMMER));
        assertTrue(gyldigPostnummer.evaluate("0357"));
    }

    @Override
    public String getPoststed(String postnummer) {
        if (IKKE_EKSISTERENDE_POSTNUMMER.equals(postnummer)) {
            return null;
        } else {
            return "Oslo";
        }
    }

    @Override
    public String getLand(String landkode) {
        fail("skal ikke kalles i.f.m. postnummervalidering");
        return null;
    }
}
