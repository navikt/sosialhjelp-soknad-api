package no.nav.sosialhjelp.soknad.consumer.bostotte;

import no.nav.sosialhjelp.soknad.web.selftest.Pingable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class BostotteConfigTest {
    @Test
    public void bostotteConfig_verifiserStandardverdier() {
        BostotteConfig bostotteConfig = new BostotteConfig();

        assertThat(bostotteConfig.getBostotteImpl()).isNotNull();
        assertThat(bostotteConfig.getUri()).isEqualToIgnoringCase("");
    }

    @Test
    public void bostotteConfig_verifiserPingOppretting() {
        BostotteConfig bostotteConfig = new BostotteConfig();

        Pingable pingable = bostotteConfig.opprettHusbankenPing();
        assertThat(pingable).isNotNull();
    }
}