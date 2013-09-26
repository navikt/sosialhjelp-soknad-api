package no.nav.sbl.dialogarena.adresse;

import org.junit.Test;

import static no.nav.sbl.dialogarena.adresse.GyldigHusbokstav.BOKSTAV;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GyldigHusbokstavTest {

    @Test
    public void skalGodtaStoreOgSmaaBokstaver() {
        assertTrue(BOKSTAV.evaluate("A"));
        assertTrue(BOKSTAV.evaluate("a"));
    }

    @Test
    public void skalGodtaNorskeBokstaver() {
        assertTrue(BOKSTAV.evaluate("Æ"));
        assertTrue(BOKSTAV.evaluate("å"));
    }

    @Test
    public void skalIkkeGodtTall() {
        assertFalse(BOKSTAV.evaluate("5"));
    }
}
