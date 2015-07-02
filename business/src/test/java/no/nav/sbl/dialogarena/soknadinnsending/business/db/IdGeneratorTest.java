package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import org.junit.Assert;
import org.junit.Test;

public class IdGeneratorTest {
    
    @Test
    public void skalGenererId() {
        String behandlingsId = IdGenerator.lagBehandlingsId(1l);
        Assert.assertEquals("100000001", behandlingsId);
    }
    
    @Test(expected=RuntimeException.class)
    public void skalFaaFeilVedForHoyId() {
        String behandlingsId = IdGenerator.lagBehandlingsId(10000000000000l);
        Assert.assertTrue(true);
    }
}
