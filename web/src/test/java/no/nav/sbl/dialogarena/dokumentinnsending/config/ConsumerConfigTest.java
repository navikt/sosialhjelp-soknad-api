package no.nav.sbl.dialogarena.dokumentinnsending.config;

import no.nav.sbl.dialogarena.common.kodeverk.config.KodeverkConfig;
import no.nav.sbl.dialogarena.dokumentinnsending.service.BrukerBehandlingServiceIntegrationMock;
import no.nav.sbl.dialogarena.dokumentinnsending.service.DefaultSoknadService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = KodeverkConfig.class)
public class ConsumerConfigTest {

    @Bean
    public DefaultSoknadService soknadService() {
        return new DefaultSoknadService();
    }

    @Bean
    public BrukerBehandlingServiceIntegrationMock brukerBehandlingServiceIntegration() {
        return new BrukerBehandlingServiceIntegrationMock();
    }
}