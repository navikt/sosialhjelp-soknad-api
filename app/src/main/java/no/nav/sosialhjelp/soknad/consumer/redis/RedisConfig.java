package no.nav.sosialhjelp.soknad.consumer.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

import static org.slf4j.LoggerFactory.getLogger;

@Profile("!no-redis")
@Configuration
public class RedisConfig {

    private static final Logger log = getLogger(RedisConfig.class);
    private static final long TIMEOUT_SECONDS = 1;

    @Value("${redis_host}")
    private String host;

    @Value("${redis_port}")
    private int port;

    @Value("${redis_password}")
    private String password;

    @Bean
    public RedisClient redisClient() {
        RedisURI redisURI = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withPassword(password)
                .withTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();

        return RedisClient.create(redisURI);
    }

    @Bean
    public RedisStore redisStore(RedisClient redisClient) {
        return new RedisStore(redisClient);
    }

    @Bean
    public RedisService redisService(RedisStore redisStore) {
        log.info("Starter RedisService");
        return new RedisServiceImpl(redisStore);
    }

}
