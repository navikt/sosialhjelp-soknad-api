package no.nav.sbl.dialogarena.adresse;

import org.junit.Test;

import static no.nav.sbl.dialogarena.adresse.GyldigBolignummer.LHUK_OG_4_SIFFER;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GyldigBolignummerTest {

    @Test
    public void gyldigeBokstaverErLHUK() {
        assertTrue(LHUK_OG_4_SIFFER.evaluate("L1234"));
        assertTrue(LHUK_OG_4_SIFFER.evaluate("H1234"));
        assertTrue(LHUK_OG_4_SIFFER.evaluate("U1234"));
        assertTrue(LHUK_OG_4_SIFFER.evaluate("K1234"));
        assertFalse(LHUK_OG_4_SIFFER.evaluate("M1234"));
    }

    @Test
    public void skalHaAkkuratFemTegn() {
        assertFalse(LHUK_OG_4_SIFFER.evaluate("L123"));
        assertFalse(LHUK_OG_4_SIFFER.evaluate("L12345"));
    }

    @Test
    public void bokstavSkalStaaFoerst() {
        assertFalse(LHUK_OG_4_SIFFER.evaluate("1L234"));
        assertFalse(LHUK_OG_4_SIFFER.evaluate("12L34"));
        assertFalse(LHUK_OG_4_SIFFER.evaluate("123L4"));
        assertFalse(LHUK_OG_4_SIFFER.evaluate("1234L"));
    }

    @Test
    public void fireTallSkalStaaTilSlutt() {
        assertFalse(LHUK_OG_4_SIFFER.evaluate("LL234"));
        assertFalse(LHUK_OG_4_SIFFER.evaluate("LLL34"));
        assertFalse(LHUK_OG_4_SIFFER.evaluate("LLLL4"));
        assertFalse(LHUK_OG_4_SIFFER.evaluate("LLLLL"));
    }
}
