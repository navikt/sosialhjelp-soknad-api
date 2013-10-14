package no.nav.sbl.dialogarena.websoknad.domain;

import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PersonAlderTest {

    // 02.10.2013
    private static final long IDAG = 1***REMOVED***2L;

    @Before
    public void init() {
        DateTimeUtils.setCurrentMillisFixed(IDAG);
    }

    @Test
    public void alderSkalVaere100ForPersonMedFNR_***REMOVED***() {
        PersonAlder alder = new PersonAlder("***REMOVED***");
        assertEquals(100, alder.getAlder());
    }

    @Test(expected = IllegalArgumentException.class)
    public void skalFaaExceptionDersomFnrErUgyldig() {
        PersonAlder alder = new PersonAlder("***REMOVED***");
        assertEquals(100, alder.getAlder());
    }

    @Test
    public void alderSkalVaere105ForPersonMedFNR_***REMOVED***() {
        PersonAlder alder = new PersonAlder("***REMOVED***");
        assertEquals(105, alder.getAlder());
    }

    @Test
    public void alderSkalVaere5ForPersonMedFNR_***REMOVED***() {
        PersonAlder alder = new PersonAlder("***REMOVED***");
        assertEquals(5, alder.getAlder());
    }

    @Test
    public void skalReturnere66AarForPersonSomFyller67AarDenneMaaneden() {
        PersonAlder alder = new PersonAlder("***REMOVED***");
        assertEquals(66, alder.getAlder());
    }

    @Test
    public void skalReturnere67AarForPersonSomFylte67AarForrigeMaaneden() {
        long nyIdag = 1383260400000L;
        DateTimeUtils.setCurrentMillisFixed(nyIdag);

        PersonAlder alder = new PersonAlder("***REMOVED***");
        assertEquals(67, alder.getAlder());
    }

    @Test
    public void alderSkalVaere100ForPersonMedDNummer_46071175367() {
        PersonAlder alder = new PersonAlder("46071175367");
        assertEquals(2, alder.getAlder());
    }

}
