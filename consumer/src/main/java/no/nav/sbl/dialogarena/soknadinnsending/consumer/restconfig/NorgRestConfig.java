package no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig;

import no.nav.sbl.dialogarena.redis.RedisService;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.norg.NorgConsumerMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.norg.NorgConsumerImpl;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.sbl.rest.RestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;


@Configuration
public class NorgRestConfig {

    public static final String NORG_KEY = "start.norg.withmock";

    @Value("${norg.rest.url}")
    private String endpoint;

    @Bean
    public NorgConsumer norgConsumer(RedisService redisService) {
        NorgConsumer prod = new NorgConsumerImpl(RestUtils.createClient(), endpoint, redisService);
        NorgConsumer mock = new NorgConsumerMock().norgConsumerMock();
        return createSwitcher(prod, mock, NORG_KEY, NorgConsumer.class);
    }
    
    @Bean
    public Pingable norgRestPing(NorgConsumer norgConsumer) {
        return () -> {
            PingMetadata metadata = new PingMetadata(endpoint, "Norg2", false);
            try {
                norgConsumer.ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }
}
