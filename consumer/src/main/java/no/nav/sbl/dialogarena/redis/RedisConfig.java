package no.nav.sbl.dialogarena.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
public class RedisConfig {

    private static final Logger log = getLogger(RedisConfig.class);

    @Value("${redis_mocked}")
    private boolean mocked;

    @Value("${redis_host}")
    private String host;

    @Value("${redis_port}")
    private int port;

    @Value("${redis_password}")
    private String password;

    @Bean
    public RedisClient redisClient() {
        if (mocked) {
            RedisMockUtil.startRedisMocked(port);
        }

        RedisURI redisURI = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withPassword(password)
                .build();

        return RedisClient.create(redisURI);
    }

    @Bean
    public RedisStore redisStore(RedisClient redisClient) {
        return new RedisStore(redisClient);
    }
}
