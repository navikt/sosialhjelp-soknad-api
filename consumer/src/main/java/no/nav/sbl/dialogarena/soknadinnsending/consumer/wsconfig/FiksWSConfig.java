package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.ks.svarut.servicesv9.ForsendelsesServiceV9;
import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.fiks.ForsendelseServiceMock;
import no.nav.sbl.dialogarena.types.Pingable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static no.nav.metrics.MetricsFactory.createTimerProxyForWebService;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class FiksWSConfig {

    @Value("${fiks.svarut.url}")
    private String fiksEndpoint;

    private final int RECEIVE_TIMEOUT = 10 * 60_000;
    private final int CONNECTION_TIMEOUT = 10_000;

    private ForsendelsesServiceV9 fiksClient() {
        return new CXFClient<>(ForsendelsesServiceV9.class)
                .address(fiksEndpoint)
                .configureStsForSystemUser()
                .timeout(RECEIVE_TIMEOUT, CONNECTION_TIMEOUT)
                .build();
    }

    @Bean
    public ForsendelsesServiceV9 forsendelsesServiceV9() {
        if (MockUtils.isTillatMockRessurs()) {
            return new ForsendelseServiceMock().forsendelseMock();
        }

        return createTimerProxyForWebService("FiksForsendelse", fiksClient(), ForsendelsesServiceV9.class);
    }

    @Bean
    public Pingable forsendelsePing() {
        return new Pingable() {
            Ping.PingMetadata metadata = new Ping.PingMetadata(UUID.randomUUID().toString(), fiksEndpoint,"Fiks_v9", false);

            @Override
            public Ping ping() {
                try {
                    fiksClient().retreiveForsendelseTyper();
                    return lyktes(metadata);
                } catch (Exception e) {
                    return feilet(metadata, e);
                }
            }
        };
    }

}
