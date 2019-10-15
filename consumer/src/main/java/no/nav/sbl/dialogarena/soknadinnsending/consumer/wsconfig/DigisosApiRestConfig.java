package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi.DigisosApi;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi.DigisosApiImpl;
import no.nav.sbl.dialogarena.types.Pingable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;
import static org.mockito.Mockito.mock;

@Configuration
public class DigisosApiRestConfig {
    @Value("${digisos_api_baseurl}")
    private String digisosApiEndpoint;

    public static final String DIGISOSAPI_WITHMOCK = "start.digisosapi.withmock";

    @Bean
    public DigisosApi digisosApi() {
        DigisosApi mock = mock(DigisosApi.class);
        DigisosApi prod = new DigisosApiImpl();
        return createMetricsProxyWithInstanceSwitcher("DigisosApi", prod, mock, DIGISOSAPI_WITHMOCK, DigisosApi.class);
    }

    @Bean
    public Pingable digisosPing() {
        return new Pingable() {
            Ping.PingMetadata metadata = new Ping.PingMetadata(digisosApiEndpoint, "DigisosApi", false);

            @Override
            public Ping ping() {
                try {
                    digisosApi().ping();
                    return lyktes(metadata);
                } catch (Exception e) {
                    return feilet(metadata, e);
                }
            }
        };
    }
}
