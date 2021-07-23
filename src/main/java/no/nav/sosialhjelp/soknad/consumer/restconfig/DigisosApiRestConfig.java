package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.nav.sosialhjelp.soknad.consumer.fiks.DigisosApi;
import no.nav.sosialhjelp.soknad.consumer.fiks.DigisosApiImpl;
import no.nav.sosialhjelp.soknad.consumer.fiks.DigisosApiProperties;
import no.nav.sosialhjelp.soknad.consumer.fiks.KommuneInfoService;
import no.nav.sosialhjelp.soknad.consumer.redis.RedisService;
import no.nav.sosialhjelp.soknad.web.selftest.Pingable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sosialhjelp.metrics.MetricsFactory.createTimerProxyForWebService;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.feilet;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.lyktes;

@Configuration
public class DigisosApiRestConfig {

    @Value("${digisos_api_baseurl}")
    private String digisosApiEndpoint;
    @Value("${idporten_token_url}")
    private String idPortenTokenUrl;
    @Value("${idporten_clientid}")
    private String idPortenClientId;
    @Value("${idporten_scope}")
    private String idPortenScope;
    @Value("${idporten_config_url}")
    private String idPortenConfigUrl;
    @Value("${integrasjonsid_fiks}")
    private String integrasjonsidFiks;
    @Value("${integrasjonpassord_fiks}")
    private String integrasjonpassordFiks;
    @Value("${virksomhetssertifikat_path}")
    private String virksomhetssertifikatPath;

    @Bean
    public DigisosApi digisosApi(RedisService redisService) {
        var digisosApi = new DigisosApiImpl(digisosApiProperties(), redisService);
        return createTimerProxyForWebService("DigisosApi", digisosApi, DigisosApi.class); // timerProxyForWebService fordi metrikkene er prefixet med 'ws'. Dette kan/bør endres senere
    }

    @Bean
    public KommuneInfoService kommuneInfoService(DigisosApi digisosapi, RedisService redisService) {
        return new KommuneInfoService(digisosapi, redisService);
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
        return new DigisosApiProperties(
                digisosApiEndpoint,
                idPortenTokenUrl,
                idPortenClientId,
                idPortenScope,
                idPortenConfigUrl,
                integrasjonsidFiks,
                integrasjonpassordFiks,
                virksomhetssertifikatPath);
    }
}
