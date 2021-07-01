package no.nav.sosialhjelp.soknad.business.util;

import no.nav.sosialhjelp.soknad.domain.model.util.StatsborgerskapType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StatsborgerskapTypeTest {

    @Test
    public void skalReturnereNorskForLandkodeNOR() {
        assertThat(StatsborgerskapType.get("NOR")).isEqualTo("norsk");
    }

    @Test
    public void skalReturnereEOSForLandkodeDNK() {
        assertThat(StatsborgerskapType.get("DNK")).isEqualTo("eos");
    }

    @Test
    public void skalReturnereIkkeEosForLandkodeBUR() {
        assertThat(StatsborgerskapType.get("BUR")).isEqualTo("ikkeEos");
    }
}