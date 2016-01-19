package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.StatsborgerskapType;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class StatsborgerskapTypeTest {

    @Test
    public void skalReturnereNorskForLandkodeNOR() {
        assertThat(StatsborgerskapType.get("NOR"), equalTo("norsk"));
    }

    @Test
    public void skalReturnereEOSForLandkodeDNK() {
        assertThat(StatsborgerskapType.get("DNK"), equalTo("eos"));
    }

    @Test
    public void skalReturnereIkkeEosForLandkodeBUR() {
        assertThat(StatsborgerskapType.get("BUR"), equalTo("ikkeEos"));
    }
}