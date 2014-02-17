package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import org.junit.Assert;
import org.junit.Test;

public class EosBorgerServiceTest {
    EosBorgerService service = new EosBorgerService();
    
    @Test
    public void skalReturnereTrueForEosBorgerFraAnnetLandEnnNorge() {
        Assert.assertTrue(service.isEosLandAnnetEnnNorge("DNK"));
    }
    
    @Test
    public void skalReturnereFalseForEosBorgerFraNorge() {
        Assert.assertFalse(service.isEosLandAnnetEnnNorge("NOR"));
    }
    
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
