package no.nav.sbl.dialogarena.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RedisConfig {

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

    @Bean
    public RedisService redisService(RedisStore redisStore) {
        return new RedisService(redisStore);
    }
}
