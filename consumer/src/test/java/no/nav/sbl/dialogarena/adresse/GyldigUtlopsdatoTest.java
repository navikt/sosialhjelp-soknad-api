package no.nav.sbl.dialogarena.adresse;

import org.joda.time.LocalDate;
import org.junit.AfterClass;
import org.junit.Test;

import static org.joda.time.DateTimeUtils.setCurrentMillisFixed;
import static org.joda.time.DateTimeUtils.setCurrentMillisSystem;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class GyldigUtlopsdatoTest {

    private final GyldigUtlopsdato gyldigUtlopsdato = new GyldigUtlopsdato();

    private final LocalDate idag = new LocalDate(2013, 5, 25);

    @Test
    public void dagensDatoErGyldigUtlopsdato() {
        setCurrentMillisFixed(idag.toDate().getTime());
        assertTrue(gyldigUtlopsdato.evaluate(idag));
    }

    @Test
    public void igaarErIkkeGyldigUtlopsdato() {
        setCurrentMillisFixed(idag.toDate().getTime());
        assertFalse(gyldigUtlopsdato.evaluate(idag.minusDays(1)));
    }

    @Test
    public void ettAarFremITidFraOgMedDagensDatoErGyldigUtlopsdato() {
        setCurrentMillisFixed(idag.toDate().getTime());
        assertTrue(gyldigUtlopsdato.evaluate(new LocalDate(idag.getYear() + 1, idag.getMonthOfYear(), idag.getDayOfMonth() - 1)));
    }

    @Test
    public void sammeDatoOmEtAarErIkkeGyldigUtlopsdato() {
        setCurrentMillisFixed(idag.toDate().getTime());
        assertFalse(gyldigUtlopsdato.evaluate(new LocalDate(idag.getYear() + 1, idag.getMonthOfYear(), idag.getDayOfMonth())));
    }

    @Test
    public void ettAarOgEnDagFremITidErIkkeGyldigUtlopsdato() {
        setCurrentMillisFixed(idag.toDate().getTime());
        assertFalse(gyldigUtlopsdato.evaluate(new LocalDate(idag.getYear() + 1, idag.getMonthOfYear(), idag.getDayOfMonth() + 1)));
    }


    @AfterClass
    public static void resetToSystemClock() {
        setCurrentMillisSystem();
    }
}
