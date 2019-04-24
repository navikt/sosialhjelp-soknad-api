package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import org.junit.Test;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.tilIntegerMedAvrunding;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class JsonUtilsTest {

    @Test
    public void tilIntegerMedAvrundingReturnererNullVedTomStreng() {
        assertThat(tilIntegerMedAvrunding(""), is(nullValue()));
    }

    @Test
    public void tilIntegerMedAvrundingRunderNed() {
        String s = "15.2";

        assertThat(tilIntegerMedAvrunding(s), is(15));
    }

    @Test
    public void tilIntegerMedAvrundingRunderOpp() {
        String s = "15.6";

        assertThat(tilIntegerMedAvrunding(s), is(16));
    }

    @Test
    public void tilIntegerMedAvrundingTaklerStrengMedWhitespace() {
        String s = "1 505.32";

        assertThat(tilIntegerMedAvrunding(s), is(1505));
    }

    @Test
    public void tilIntegerMedAvrundingTaklerStrengUtenDesimaler() {
        String s = "150";

        assertThat(tilIntegerMedAvrunding(s), is(150));
    }

    @Test
    public void tilIntegerMedAvrundingTaklerStrengMedKomma() {
        String s = "1 520,00";

        assertThat(tilIntegerMedAvrunding(s), is(1520));
    }

    @Test
    public void tilIntegerMedAvrundingTaklerStrengMedNonBreakingSpace() {
        String s = "3 880,00";

        assertThat(tilIntegerMedAvrunding(s), is(3880));
    }
}