import no.nav.sbl.dialogarena.websoknad.domain.Alder;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: I140481
 * Date: 01.10.13
 * Time: 10:29
 * To change this template use File | Settings | File Templates.
 */
public class AlderTest {
    @Test
    public void alderSkalVaere24ForPersonMedFNR_08122413838() {
        Alder alder = new Alder("08122413838");
        assertEquals(24, alder.getAlder());
    }
}
