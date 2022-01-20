//package no.nav.sosialhjelp.soknad.web.server;
//
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
//
//@Disabled
//class SoknadsosialhjelpServerTest {
//
//    @Test
//    void withEnvironmentVariableExpansion_shouldBeUnchanged() {
//        assertUnchanged(null);
//        assertUnchanged("");
//        assertUnchanged("https://dette.er.ett.api.no");
//        assertUnchanged("{}");
//        assertUnchanged("{2}");
//        assertUnchanged("{https://dette.er.ett.api.no}");
//        assertUnchanged("https://dette.er.ett.api.no}");
//        assertUnchanged("${https://dette.er.ett.api.no");
//        assertUnchanged("$https://dette.er.ett.api.no");
//        assertUnchanged("blalba {} ok {2} $ tja ${...");
//    }
//
//    @Test
//    void withEnvironmentVariableExpansion_notRequired_shouldHandleDefaultValues() {
//        System.setProperty("ENV_VAR_SET", "envVar");
//
//        String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("${ENV_VAR_SET:en.ny:fancy:variabel!=}", false);
//        assertThat(value).isEqualTo("envVar");
//
//        value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("${ENV_VAR_NOT_SET:en.ny:fancy:variabel!=}", false);
//        assertThat(value).isEqualTo("en.ny:fancy:variabel!=");
//    }
//
//    @Test
//    void withEnvironmentVariableExpansion_withRequiredParameterAndEnvVarSet_shouldHandleDefaultValues() {
//        System.setProperty("ENV_VAR_SET", "envVar");
//
//        String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("${ENV_VAR_SET:en.ny:fancy:variabel!=}", true);
//        assertThat(value).isEqualTo("envVar");
//    }
//
//    @Test
//    void withEnvironmentVariableExpansion_withRequiredWithoutEnvVarSet_ShouldUseVariable() {
//        String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("${ENV_NOT_SET:en.ny:fancy:variabel!=}", true);
//        assertThat(value).isEqualTo("en.ny:fancy:variabel!=");
//    }
//
//    @Test
//    void withEnvironmentVariableExpansion_withRequiredWithoutEnvVarSet_ShouldThrowException() {
//        assertThatExceptionOfType(IllegalStateException.class)
//                .isThrownBy(() -> SoknadsosialhjelpServer.withEnvironmentVariableExpansion("${ENV_NOT_SET}", true));
//    }
//
//    @Test
//    void withEnvironmentVariableExpansion_shouldHandleMultipleProperties() {
//        System.setProperty("SoknadsosialhjelpServerTest.property", "foobar");
//
//        String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("Test ${SoknadsosialhjelpServerTest.property} med to ${SoknadsosialhjelpServerTest.property}", true);
//        assertThat(value).isEqualTo("Test foobar med to foobar");
//
//        value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion("Test ${SoknadsosialhjelpServerTest.property} med to ${SoknadsosialhjelpServerTest.property}", false);
//        assertThat(value).isEqualTo("Test foobar med to foobar");
//    }
//
//
//    private void assertUnchanged(String input) {
//        String value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion(input, true);
//        assertThat(value).isEqualTo(input);
//
//        value = SoknadsosialhjelpServer.withEnvironmentVariableExpansion(input, false);
//        assertThat(value).isEqualTo(input);
//    }
//}
