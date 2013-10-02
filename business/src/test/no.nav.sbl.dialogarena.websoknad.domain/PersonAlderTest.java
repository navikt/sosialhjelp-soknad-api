import no.nav.sbl.dialogarena.websoknad.domain.PersonAlder;
import static org.junit.Assert.assertEquals;

import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

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
}
