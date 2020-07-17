package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.ks.svarut.servicesv9.ForsendelsesServiceV9;
import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.fiks.ForsendelseServiceMock;
import no.nav.sbl.dialogarena.types.Pingable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class FiksWSConfig {

    public static final String FIKS_KEY = "start.fiks.withmock";

    @Value("${fiks.svarut.url}")
    private String fiksEndpoint;

    /*private ServiceBuilder<ForsendelsesServiceV9>.PortTypeBuilder<ForsendelsesServiceV9> factory() {
        final int receiveTimeout = 10 * 60_000;
        final int connectionTimeout = 10_000;

        return new ServiceBuilder<>(ForsendelsesServiceV9.class)
                .asStandardService()
                .withTimeout(receiveTimeout, connectionTimeout)
                .withAddress(fiksEndpoint)
                .withWsdl("classpath:/wsdl/svarUt.wsdl")
                .build()
                .withHttpsMock();
    }*/

    @Bean
    public ForsendelsesServiceV9 forsendelsesServiceV9() {
        final int receiveTimeout = 10 * 60_000;
        final int connectionTimeout = 10_000;
        return new CXFClient<>(ForsendelsesServiceV9.class).address(fiksEndpoint).configureStsForSubject().timeout(connectionTimeout, receiveTimeout).build();
/*
        ForsendelsesServiceV9 mock = new ForsendelseServiceMock().forsendelseMock();
        ForsendelsesServiceV9 prod = factory().withSystemSecurity().get();
        return createMetricsProxyWithInstanceSwitcher("FiksForsendelse", prod, mock, FIKS_KEY, ForsendelsesServiceV9.class);*/
    }

    @Bean
    public Pingable forsendelsePing() {
        final int receiveTimeout = 10 * 60_000;
        final int connectionTimeout = 10_000;

        ForsendelsesServiceV9 selftestClient = new CXFClient<>(ForsendelsesServiceV9.class).address(fiksEndpoint).configureStsForSystemUser().timeout(connectionTimeout, receiveTimeout).build();

        return new Pingable() {
            Ping.PingMetadata metadata = new Ping.PingMetadata(fiksEndpoint,"Fiks_v9", false);

            @Override
            public Ping ping() {
                try {
                    selftestClient.retreiveForsendelseTyper();
                    return lyktes(metadata);
                } catch (Exception e) {
                    return feilet(metadata, e);
                }
            }
        };
    }

}
