package no.nav.sbl.dialogarena.konto;

import org.apache.commons.collections15.Predicate;
import org.junit.Test;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class GyldigUtenlandskKontonummerTest {

    private Predicate<String> gyldigKontonummer = GyldigUtenlandskKontonummer.MINDRE_ELLER_AKKURAT_36_TEGN;

    @Test
    public void godtarBlankeKontonummer() {
        assertTrue(gyldigKontonummer.evaluate(null));
        assertTrue(gyldigKontonummer.evaluate(""));
    }

    @Test
    public void ingenSpesielleBegrensningerUtoverMaksAntallTegn() {
        assertTrue(gyldigKontonummer.evaluate("1a-#/()"));
        assertTrue(gyldigKontonummer.evaluate(randomNumeric(36)));

        assertFalse(gyldigKontonummer.evaluate(randomNumeric(37)));
    }
}
