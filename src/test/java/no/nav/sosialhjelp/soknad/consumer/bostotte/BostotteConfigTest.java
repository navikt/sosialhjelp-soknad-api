package no.nav.sosialhjelp.soknad.consumer.bostotte;

import no.nav.sosialhjelp.soknad.web.selftest.Pingable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BostotteConfigTest {
    @Test
    void bostotteConfig_verifiserStandardverdier() {
        BostotteConfig bostotteConfig = new BostotteConfig();

        assertThat(bostotteConfig.getBostotteImpl()).isNotNull();
        assertThat(bostotteConfig.getUri()).isEqualToIgnoringCase("");
    }

    @Test
    void bostotteConfig_verifiserPingOppretting() {
        BostotteConfig bostotteConfig = new BostotteConfig();

        Pingable pingable = bostotteConfig.opprettHusbankenPing();
        assertThat(pingable).isNotNull();
    }
}