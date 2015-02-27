package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import org.junit.Assert;
import org.junit.Test;

public class LandServiceTest {
    LandService service = new LandService();
    
    @Test
    public void skalReturnereNorskForNorskLandkode() {
        Assert.assertEquals("norsk", service.getStatsborgeskapType("NOR"));
    }
    
    @Test
    public void skalReturnereEosForDanskLandkode() {
        Assert.assertEquals("eos", service.getStatsborgeskapType("DNK"));
    }
    
    @Test
    public void skalReturnereikkeEosForOmanskLandkode() {
        Assert.assertEquals("ikkeEos", service.getStatsborgeskapType("OMN"));
    }
}
