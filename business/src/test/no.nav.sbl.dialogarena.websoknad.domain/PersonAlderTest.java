import no.nav.sbl.dialogarena.websoknad.domain.PersonAlder;
import static org.junit.Assert.assertEquals;

import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: I140481
 * Date: 01.10.13
 * Time: 10:29
 * To change this template use File | Settings | File Templates.
 */
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
}
