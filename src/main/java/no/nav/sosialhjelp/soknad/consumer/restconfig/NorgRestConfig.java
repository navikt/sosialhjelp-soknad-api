package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.nav.sbl.rest.RestUtils;
import no.nav.sosialhjelp.soknad.consumer.norg.NorgConsumerImpl;
import no.nav.sosialhjelp.soknad.consumer.redis.RedisService;
import no.nav.sosialhjelp.soknad.domain.model.norg.NorgConsumer;
import no.nav.sosialhjelp.soknad.mock.norg.NorgConsumerMock;
import no.nav.sosialhjelp.soknad.web.types.Pingable;
import no.nav.sosialhjelp.soknad.web.types.Pingable.Ping.PingMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createSwitcher;
import static no.nav.sosialhjelp.soknad.web.types.Pingable.Ping.feilet;
import static no.nav.sosialhjelp.soknad.web.types.Pingable.Ping.lyktes;


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
