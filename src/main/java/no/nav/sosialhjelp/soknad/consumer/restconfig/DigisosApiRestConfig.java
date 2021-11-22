package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.nav.sosialhjelp.soknad.client.fiks.KommuneInfoService;
import no.nav.sosialhjelp.soknad.consumer.fiks.DigisosApi;
import no.nav.sosialhjelp.soknad.consumer.fiks.DigisosApiImpl;
import no.nav.sosialhjelp.soknad.consumer.fiks.DigisosApiProperties;
import no.nav.sosialhjelp.soknad.web.selftest.Pingable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sosialhjelp.metrics.MetricsFactory.createTimerProxy;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.feilet;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.lyktes;

@Configuration
public class DigisosApiRestConfig {

    @Value("${digisos_api_baseurl}")
    private String digisosApiEndpoint;
    @Value("${integrasjonsid_fiks}")
    private String integrasjonsidFiks;
    @Value("${integrasjonpassord_fiks}")
    private String integrasjonpassordFiks;

    @Bean
    public DigisosApi digisosApi(KommuneInfoService kommuneInfoService) {
        var digisosApi = new DigisosApiImpl(digisosApiProperties(), kommuneInfoService);
        return createTimerProxy("DigisosApi", digisosApi, DigisosApi.class);
    }

    @Bean
    public Pingable digisosPing(DigisosApi digisosApi) {
        return new Pingable() {
            Ping.PingMetadata metadata = new Ping.PingMetadata(digisosApiEndpoint, "DigisosApi", false);

            @Override
            public Ping ping() {
                try {
                    digisosApi.ping();
                    return lyktes(metadata);
                } catch (Exception e) {
                    return feilet(metadata, e);
                }
            }
        };
    }

    private DigisosApiProperties digisosApiProperties() {
        return new DigisosApiProperties(digisosApiEndpoint, integrasjonsidFiks, integrasjonpassordFiks);
    }
}
