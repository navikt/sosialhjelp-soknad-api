package no.nav.sosialhjelp.soknad.web.server;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(value).isEqualTo("envVar");

        value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("${ENV_VAR_NOT_SET:en.ny:fancy:variabel!=}", false);
        assertThat(value).isEqualTo("en.ny:fancy:variabel!=");
    }

    @Test
    public void withEnvironmentVariableExpansion_withRequiredParameterAndEnvVarSet_shouldHandleDefaultValues() {
        System.setProperty("ENV_VAR_SET", "envVar");

        String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("${ENV_VAR_SET:en.ny:fancy:variabel!=}", true);
        assertThat(value).isEqualTo("envVar");
    }

    @Test
    public void withEnvironmentVariableExpansion_withRequiredWithoutEnvVarSet_ShouldUseVariable() {
        String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("${ENV_NOT_SET:en.ny:fancy:variabel!=}", true);
        assertThat(value).isEqualTo("en.ny:fancy:variabel!=");
    }

    @Test(expected = IllegalStateException.class)
    public void withEnvironmentVariableExpansion_withRequiredWithoutEnvVarSet_ShouldThrowException() {
        String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("${ENV_NOT_SET}", true);
        assertThat(value).isEqualTo("foobar");
    }

    @Test
    public void withEnvironmentVariableExpansion_shouldHandleMultipleProperties() {
        System.setProperty("SoknadsosialhjelpServerTest.property", "foobar");

        String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("Test ${SoknadsosialhjelpServerTest.property} med to ${SoknadsosialhjelpServerTest.property}", true);
        assertThat(value).isEqualTo("Test foobar med to foobar");

        value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("Test ${SoknadsosialhjelpServerTest.property} med to ${SoknadsosialhjelpServerTest.property}", false);
        assertThat(value).isEqualTo("Test foobar med to foobar");
    }


    private void assertUnchanged(String input) {
        String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion(input, true);
        assertThat(value).isEqualTo(input);

        value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion(input, false);
        assertThat(value).isEqualTo(input);
    }
}
