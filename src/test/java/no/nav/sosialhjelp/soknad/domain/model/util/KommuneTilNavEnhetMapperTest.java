package no.nav.sosialhjelp.soknad.domain.model.util;

import org.junit.Test;

import static no.nav.sosialhjelp.soknad.domain.model.util.KommuneTilNavEnhetMapper.getOrganisasjonsnummer;
import static org.assertj.core.api.Assertions.assertThat;

public class KommuneTilNavEnhetMapperTest {

    @Test
    public void TestGetOrganisasjonsnummerOfNull() {
        String result = getOrganisasjonsnummer(null);
        assertThat(result).isNull();
    }
}