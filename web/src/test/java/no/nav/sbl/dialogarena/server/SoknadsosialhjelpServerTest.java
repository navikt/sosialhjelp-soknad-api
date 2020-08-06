package no.nav.sbl.dialogarena.server;

import org.junit.Assert;
import org.junit.Test;

public class SoknadsosialhjelpServerTest {
   
    @Test
    public void withEnvironmentVariableExpansion_shouldBeUnchanged() {
        assertUnchanged(null);
        assertUnchanged("");
        assertUnchanged("https://dette.er.ett.api.no");
        assertUnchanged("{}");
        assertUnchanged("{2}");
        assertUnchanged("{https://dette.er.ett.api.no}");
        assertUnchanged("https://dette.er.ett.api.no}");
        assertUnchanged("${https://dette.er.ett.api.no");
        assertUnchanged("$https://dette.er.ett.api.no");
        assertUnchanged("blalba {} ok {2} $ tja ${...");
    }
    
    @Test
    public void withEnvironmentVariableExpansion_notRequired_shouldHandleDefaultValues() {
        System.setProperty("ENV_VAR_SET", "envVar");
        
        String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("${ENV_VAR_SET:en.ny:fancy:variabel!=}", false);
        Assert.assertEquals("envVar", value);

        value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("${ENV_VAR_NOT_SET:en.ny:fancy:variabel!=}", false);
        Assert.assertEquals("en.ny:fancy:variabel!=", value);
    }

    @Test
    public void withEnvironmentVariableExpansion_withRequiredParameterAndEnvVarSet_shouldHandleDefaultValues() {
        System.setProperty("ENV_VAR_SET", "envVar");

        String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("${ENV_VAR_SET:en.ny:fancy:variabel!=}", true);
        Assert.assertEquals("envVar", value);
    }

    @Test(expected = IllegalStateException.class)
    public void withEnvironmentVariableExpansion_withRequiredWithoutEnvVarSet_ShouldThrowException() {
        String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("${ENV_NOT_SET:en.ny:fancy:variabel!=}", true);
        Assert.assertEquals("foobar", value);
    }

    @Test
    public void withEnvironmentVariableExpansion_shouldHandleMultipleProperties() {
        System.setProperty("SoknadsosialhjelpServerTest.property", "foobar");

        String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("Test ${SoknadsosialhjelpServerTest.property} med to ${SoknadsosialhjelpServerTest.property}", true);
        Assert.assertEquals("Test foobar med to foobar", value);

        value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("Test ${SoknadsosialhjelpServerTest.property} med to ${SoknadsosialhjelpServerTest.property}", false);
        Assert.assertEquals("Test foobar med to foobar", value);
    }
    
    
    private void assertUnchanged(String input) {
        String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion(input, true);
        Assert.assertEquals(input, value);

        value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion(input, false);
        Assert.assertEquals(input, value);
    }
}
