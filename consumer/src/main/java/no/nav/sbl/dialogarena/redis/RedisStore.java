package no.nav.sbl.dialogarena.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class RedisStore {

    private final RedisCommands<String, byte[]> commands;

    @Inject
    public RedisStore(RedisClient redisClient) {
        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
        commands = connection.sync();
    }

    public byte[] get(String key) {
        return commands.get(key);
    }

    public String setex(String key, byte[] value, long timeToLiveSeconds) {
        return commands.setex(key, timeToLiveSeconds, value);
    }

    public String set(String key, byte[] value) {
        return commands.set(key, value);
    }
}
