package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte;

import no.nav.sbl.dialogarena.types.Pingable;
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

    @Test
    public void bostotteConfig_verifiserPingOppretting() {
        BostotteConfig bostotteConfig = new BostotteConfig();

        Pingable pingable = bostotteConfig.opprettHusbankenPing();
        Assertions.assertThat(pingable).isNotNull();
    }
}