package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.ks.svarut.servicesv9.ForsendelsesServiceV9;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.fiks.ForsendelseServiceMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.sbl.dialogarena.types.Pingable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;

@Configuration
public class FiksWSConfig {

    public static final String FIKS_KEY = "start.fiks.withmock";

    @Value("${fiks.svarut.url}")
    private String fiksEndpoint;

    private ServiceBuilder<ForsendelsesServiceV9>.PortTypeBuilder<ForsendelsesServiceV9> factory() {
        return new ServiceBuilder<>(ForsendelsesServiceV9.class)
                .asStandardService()
                .withAddress(fiksEndpoint)
                .withWsdl("classpath:/tjenestespesifikasjon/svarUt.wsdl")
                .build()
                .withHttpsMock();
    }

    @Bean
    public ForsendelsesServiceV9 forsendelsesServiceV9() {
        ForsendelsesServiceV9 mock = new ForsendelseServiceMock().forsendelseMock();
        ForsendelsesServiceV9 prod = factory().withUserSecurity().get();
        return createMetricsProxyWithInstanceSwitcher("FiksForsendelse", prod, mock, FIKS_KEY, ForsendelsesServiceV9.class);
    }

    @Bean
    Pingable arbeidPing() {
        ForsendelsesServiceV9 selftestEndpoint = factory().withSystemSecurity().get();
        return new Pingable() {
            @Override
            public Ping ping() {
                try {
                    selftestEndpoint.retreiveForsendelseTyper();
                    return Ping.lyktes("Fiks");
                } catch (Exception e) {
                    return Ping.feilet("Fiks", e);
                }
            }
        };
    }

}
