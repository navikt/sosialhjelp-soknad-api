package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BostotteConfigTest {
    @Test
    public void bostotteConfig_verifiserStandardverdier() {
        BostotteConfig bostotteConfig = new BostotteConfig();

        Assertions.assertThat(bostotteConfig.getBostotteImpl()).isNotNull();
        Assertions.assertThat(bostotteConfig.getUri()).isEqualToIgnoringCase("");
        Assertions.assertThat(bostotteConfig.getUsername()).isEqualToIgnoringCase("username");
        Assertions.assertThat(bostotteConfig.getAppKey()).isEqualToIgnoringCase("appKey");
    }
}