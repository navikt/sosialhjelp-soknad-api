package no.nav.sbl.dialogarena.websoknad.domain;

import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PersonAlderTest {

    // 02.10.2013
    private static final long IDAG = 1380717244702L;

    @Before
    public void init() {
        DateTimeUtils.setCurrentMillisFixed(IDAG);
    }

    @Test
    public void alderSkalVaere100ForPersonMedFNR_16051329123() {
        PersonAlder alder = new PersonAlder("16051329132");
        assertEquals(100, alder.getAlder());
    }

    @Test(expected = IllegalArgumentException.class)
    public void skalFaaExceptionDersomFnrErUgyldig() {
        PersonAlder alder = new PersonAlder("16051329332");
        assertEquals(100, alder.getAlder());
    }

    @Test
    public void alderSkalVaere105ForPersonMedFNR_06030849092() {
        PersonAlder alder = new PersonAlder("06030849092");
        assertEquals(105, alder.getAlder());
    }

    @Test
    public void alderSkalVaere5ForPersonMedFNR_12040886859() {
        PersonAlder alder = new PersonAlder("12040886859");
        assertEquals(5, alder.getAlder());
    }

    @Test
    public void skalReturnere66AarForPersonSomFyller67AarDenneMaaneden() {
        PersonAlder alder = new PersonAlder("02104635787");
        assertEquals(66, alder.getAlder());
    }

    @Test
    public void skalReturnere67AarForPersonSomFylte67AarForrigeMaaneden() {
        long nyIdag = 1383260400000L;
        DateTimeUtils.setCurrentMillisFixed(nyIdag);

        PersonAlder alder = new PersonAlder("02104635787");
        assertEquals(67, alder.getAlder());
    }

    @Test
    public void alderSkalVaere100ForPersonMedDNummer_46071175367() {
        PersonAlder alder = new PersonAlder("46071175367");
        assertEquals(2, alder.getAlder());
    }
    
    @Test
    public void alderErGodkjentForPersonSomFylte67DenneMaaneden() {
        PersonAlder alder = new PersonAlder("02104635787");
    	assertEquals(true, alder.sjekkAlder());
    }
    
    @Test
    public void alderErIkkeGodkjentForPersonSomFylte67ForrigeMaaneden() {
    	 long nyIdag = 1383260400000L;
         DateTimeUtils.setCurrentMillisFixed(nyIdag);

         PersonAlder alder = new PersonAlder("02104635787");
         assertEquals(false, alder.sjekkAlder());
    }

}
