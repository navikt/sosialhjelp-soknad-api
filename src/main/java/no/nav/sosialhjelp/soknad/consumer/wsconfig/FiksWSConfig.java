package no.nav.sosialhjelp.soknad.consumer.wsconfig;

import no.ks.svarut.servicesv9.ForsendelsesServiceV9;
import no.nav.sosialhjelp.soknad.consumer.ServiceBuilder;
import no.nav.sosialhjelp.soknad.web.selftest.Pingable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sosialhjelp.metrics.MetricsFactory.createTimerProxyForWebService;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.feilet;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.lyktes;

@Configuration
public class FiksWSConfig {

    @Value("${fiks.svarut.url}")
    private String fiksEndpoint;

    private ServiceBuilder<ForsendelsesServiceV9>.PortTypeBuilder<ForsendelsesServiceV9> factory() {
        final int receiveTimeout = 10 * 60_000;
        final int connectionTimeout = 10_000;
        
        return new ServiceBuilder<>(ForsendelsesServiceV9.class)
                .asStandardService()
                .withTimeout(receiveTimeout, connectionTimeout)
                .withAddress(fiksEndpoint)
                .withWsdl("classpath:/wsdl/svarUt.wsdl")
                .build()
                .withHttpsMock();
    }

    @Bean
    public ForsendelsesServiceV9 forsendelsesServiceV9() {
        var forsendelsesServiceV9 = factory().withSystemSecurity().get();
        return createTimerProxyForWebService("FiksForsendelse", forsendelsesServiceV9, ForsendelsesServiceV9.class);
    }

    @Bean
    public Pingable forsendelsePing() {
        ForsendelsesServiceV9 selftestEndpoint = factory().withSystemSecurity().get();
        return new Pingable() {
            Ping.PingMetadata metadata = new Ping.PingMetadata(fiksEndpoint,"Fiks_v9", false);

            @Override
            public Ping ping() {
                try {
                    selftestEndpoint.retreiveForsendelseTyper();
                    return lyktes(metadata);
                } catch (Exception e) {
                    return feilet(metadata, e);
                }
            }
        };
    }

}
