package no.nav.sbl.dialogarena.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisCommandTimeoutException;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class RedisStore {

    private static final Logger log = getLogger(RedisStore.class);

    private final RedisCommands<String, byte[]> commands;

    @Inject
    public RedisStore(RedisClient redisClient) {
        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
        commands = connection.sync();
    }

    public byte[] get(String key) {
        try {
            return commands.get(key);
        } catch (RedisCommandTimeoutException e) {
            log.warn("Redis timeout. Returnerer null.", e);
            return null;
        }
    }

    public String setex(String key, byte[] value, long timeToLiveSeconds) {
        try {
            return commands.setex(key, timeToLiveSeconds, value);
        } catch (RedisCommandTimeoutException e) {
            log.warn("Redis timeout. Returnerer null.", e);
            return null;
        }
    }

    public String set(String key, byte[] value) {
        try {
            return commands.set(key, value);
        } catch (RedisCommandTimeoutException e) {
            log.warn("Redis timeout. Returnerer null.", e);
            return null;
        }
    }
}
