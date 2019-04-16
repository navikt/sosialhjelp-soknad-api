package no.nav.sbl.dialogarena.server;

import org.junit.Assert;
import org.junit.Test;

public class SoknadsosialhjelpServerTest {
   
    @Test
    public void withEnvironmentVariableExpansionUnchanged() {
        assertUnchanged("");
        assertUnchanged(null);
        assertUnchanged(null);
        assertUnchanged("blalba {} ok {2} $ tja ${...");
    }
    
    @Test
    public void withEnvironmentVariableExpansionHandlesProperty() {
        System.setProperty("SoknadsosialhjelpServerTest.property", "foobar");
        
        String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("Test ${SoknadsosialhjelpServerTest.property} med to ${SoknadsosialhjelpServerTest.property}");
        Assert.assertEquals("Test foobar med to foobar", value);
    }
    
    
    private void assertUnchanged(String input) {
        String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion(input);
        Assert.assertEquals(input, value);
    }
}
