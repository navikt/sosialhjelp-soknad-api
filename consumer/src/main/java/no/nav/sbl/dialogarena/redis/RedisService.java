package no.nav.sbl.dialogarena.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class RedisService {

    private static final Logger log = getLogger(RedisService.class);
    private static final ObjectMapper objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper().registerModule(new KotlinModule());

    private final RedisStore redisStore;

    @Inject
    public RedisService(RedisStore redisStore) {
        this.redisStore = redisStore;
    }

    public Object get(String key, Class requestedClass) {
        byte[] value = redisStore.get(key);

        if (value != null) {
            try {
                return objectMapper.readValue(value, requestedClass);
            } catch (IOException e) {
                log.warn("Fant key={} i cache, men value var ikke {}", key, requestedClass.getSimpleName(), e);
                return null;
            }
        } else {
            return null;
        }
    }

    public void put(String key, byte[] value, long timeToLiveSeconds) {
        String set = redisStore.set(key, value, timeToLiveSeconds);
        if (set.equalsIgnoreCase("OK")) {
            log.debug("Cache put OK, key={}", key);
        } else {
            log.warn("Cache put feilet eller fikk timeout, key={}", key);
        }
    }

}
