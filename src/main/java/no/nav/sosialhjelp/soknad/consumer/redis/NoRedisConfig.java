package no.nav.sosialhjelp.soknad.consumer.redis;

import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.slf4j.LoggerFactory.getLogger;

@Profile("no-redis")
@Configuration
public class NoRedisConfig {

    private static final Logger log = getLogger(NoRedisConfig.class);

    @Bean
    public RedisService redisService() {
        log.error("Starter NoRedisService. Skal ikke skje i prod.");
        return new NoRedisService();
    }
}
