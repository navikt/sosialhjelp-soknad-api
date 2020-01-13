package no.nav.sbl.dialogarena.sendsoknad.domain.util;

import org.junit.Test;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper.getOrganisasjonsnummer;
import static org.junit.Assert.*;

public class KommuneTilNavEnhetMapperTest {

    @Test
    public void TestGetOrganisasjonsnummerOfNull() {
        String result = getOrganisasjonsnummer(null);
        assertNull(result);
    }
}