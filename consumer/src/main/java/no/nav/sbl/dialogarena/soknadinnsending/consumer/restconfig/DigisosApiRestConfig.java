package no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig;

import no.nav.sbl.dialogarena.redis.RedisService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DigisosApi;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DigisosApiImpl;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DigisosApiMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DigisosApiProperties;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.KommuneInfoService;
import no.nav.sbl.dialogarena.types.Pingable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

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

    public static final String DIGISOSAPI_WITHMOCK = "start.digisosapi.withmock";

    @Bean
    public DigisosApi digisosApi(RedisService redisService) {
        DigisosApi mock = new DigisosApiMock().digisosApiMock();
        DigisosApi prod = new DigisosApiImpl(digisosApiProperties(), redisService);
        return createMetricsProxyWithInstanceSwitcher("DigisosApi", prod, mock, DIGISOSAPI_WITHMOCK, DigisosApi.class);
    }

    @Bean
    public KommuneInfoService kommuneInfoService(DigisosApi digisosapi, RedisService redisService) {
        return new KommuneInfoService(digisosapi, redisService);
    }

    @Bean
    public Pingable digisosPing(DigisosApi digisosApi) {
        return new Pingable() {
            Ping.PingMetadata metadata = new Ping.PingMetadata(digisosApiEndpoint, "DigisosApi", true);

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
        return new DigisosApiProperties(digisosApiEndpoint, idPortenTokenUrl, idPortenClientId, idPortenScope, idPortenConfigUrl, integrasjonsidFiks, integrasjonpassordFiks);
    }
}
