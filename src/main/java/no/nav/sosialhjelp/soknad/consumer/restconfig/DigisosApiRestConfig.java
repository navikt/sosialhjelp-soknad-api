package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneInfoService;
import no.nav.sosialhjelp.soknad.consumer.fiks.DigisosApi;
import no.nav.sosialhjelp.soknad.consumer.fiks.DigisosApiImpl;
import no.nav.sosialhjelp.soknad.consumer.fiks.DigisosApiProperties;
import no.nav.sosialhjelp.soknad.health.selftest.Pingable;
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dokumentlager.DokumentlagerClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sosialhjelp.metrics.MetricsFactory.createTimerProxy;

@Configuration
public class DigisosApiRestConfig {

    @Value("${digisos_api_baseurl}")
    private String digisosApiEndpoint;
    @Value("${integrasjonsid_fiks}")
    private String integrasjonsidFiks;
    @Value("${integrasjonpassord_fiks}")
    private String integrasjonpassordFiks;

    @Bean
    public DigisosApi digisosApi(KommuneInfoService kommuneInfoService, DokumentlagerClient dokumentlagerClient) {
        var digisosApi = new DigisosApiImpl(digisosApiProperties(), kommuneInfoService, dokumentlagerClient);
        return createTimerProxy("DigisosApi", digisosApi, DigisosApi.class);
    }

    @Bean
    public Pingable digisosPing(DigisosApi digisosApi) {
        return new Pingable() {
            PingMetadata metadata = new PingMetadata(digisosApiEndpoint, "DigisosApi", false);

            @Override
            public Ping ping() {
                try {
                    digisosApi.ping();
                    return Pingable.Companion.lyktes(metadata);
                } catch (Exception e) {
                    return Pingable.Companion.feilet(metadata, e);
                }
            }
        };
    }

    private DigisosApiProperties digisosApiProperties() {
        return new DigisosApiProperties(digisosApiEndpoint, integrasjonsidFiks, integrasjonpassordFiks);
    }
}
