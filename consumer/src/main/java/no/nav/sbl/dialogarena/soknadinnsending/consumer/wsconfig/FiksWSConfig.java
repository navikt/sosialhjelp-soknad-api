package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.ks.svarut.servicesv9.ForsendelsesServiceV9;
import no.ks.svarut.servicesv9.ForsendelsesServiceV9_Service;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.fiks.ForsendelseServiceMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
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

    private ServiceBuilder<ForsendelsesServiceV9>.PortTypeBuilder<ForsendelsesServiceV9> factory() {
        final int receiveTimeout = 10 * 60_000;
        final int connectionTimeout = 10_000;
        
        return new ServiceBuilder<>(ForsendelsesServiceV9.class)
                .asStandardService()
                .withTimeout(receiveTimeout, connectionTimeout)
                .withAddress(fiksEndpoint)
                .withWsdl("classpath:/tjenestespesifikasjon/svarUt.wsdl")
//                .withWsdl("classpath:/wsdl/svarUt.wsdl") // endre når PR til tjenestespesifikasjoner er godkjent og ny versjon er ute
                .build()
                .withHttpsMock();
    }

    @Bean
    public ForsendelsesServiceV9 forsendelsesServiceV9() {
        ForsendelsesServiceV9 mock = new ForsendelseServiceMock().forsendelseMock();
        ForsendelsesServiceV9 prod = factory().withSystemSecurity().get();
        return createMetricsProxyWithInstanceSwitcher("FiksForsendelse", prod, mock, FIKS_KEY, ForsendelsesServiceV9.class);
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
