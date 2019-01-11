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
        
        final String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("Test ${SoknadsosialhjelpServerTest.property} med to ${SoknadsosialhjelpServerTest.property}");
        Assert.assertEquals("Test foobar med to foobar", value);
    }

    @Test
    public void withEnvironmentVariableExpansionHandlesDefaultValueNotBeingUsed() {
        System.setProperty("SoknadsosialhjelpServerTest.property", "foobar");

        final String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("Test ${SoknadsosialhjelpServerTest.property:2} med to ${SoknadsosialhjelpServerTest.property:lala}");
        Assert.assertEquals("Test foobar med to foobar", value);
    }

    @Test
    public void withEnvironmentVariableExpansionHandlesDefaultValueBeingUsed() {
        final String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("Test ${SoknadsosialhjelpServerTest.nonExistantProperty:2} med to ${SoknadsosialhjelpServerTest.nonExistantPropert:lala}");
        Assert.assertEquals("Test 2 med to lala", value);
    }
    
    
    private void assertUnchanged(String input) {
        final String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion(input);
        Assert.assertEquals(input, value);
    }
}
