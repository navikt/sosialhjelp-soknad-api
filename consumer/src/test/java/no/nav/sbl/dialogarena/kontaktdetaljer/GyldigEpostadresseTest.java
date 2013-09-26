package no.nav.sbl.dialogarena.kontaktdetaljer;

import org.junit.Test;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class GyldigEpostadresseTest {

    private final GyldigEpostadresse gyldigEpost = new GyldigEpostadresse();

    @Test
    public void godtarMinimalEpostadresse() {
        assertTrue(gyldigEpost.evaluate("x@y"));
    }

    @Test
    public void godtarVanligEpostadresse() {
        assertTrue(gyldigEpost.evaluate("navn@domene.tld"));
    }

    @Test
    public void godtarEpostMedSpesialtegn() {
        assertTrue(gyldigEpost.evaluate("navn+tag@gmail.com"));
    }

    @Test
    public void godtarIkkeEpostadresserLengreEnn50Tegn() {
        String epostMed50Tegn = randomAlphanumeric(48) + "@x";
        assertTrue(gyldigEpost.evaluate(epostMed50Tegn));
        assertFalse(gyldigEpost.evaluate(epostMed50Tegn + "y"));
    }

    @Test
    public void godtarIkkeEpostUtenAlfakroll() {
        assertFalse(gyldigEpost.evaluate("abcde"));
    }

    @Test
    public void godtarIkkeAlfakrollSomForsteTegn() {
        assertFalse(gyldigEpost.evaluate("@gmail.com"));
        assertFalse(gyldigEpost.evaluate("@a"));
    }

    @Test
    public void godtarIkkeAlftakrollSomSisteTegn() {
        assertFalse(gyldigEpost.evaluate("gmail@"));
        assertFalse(gyldigEpost.evaluate("a@"));
    }
}
