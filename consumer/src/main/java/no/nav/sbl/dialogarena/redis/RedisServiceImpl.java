package no.nav.sbl.dialogarena.redis;

import no.nav.sosialhjelp.api.fiks.KommuneInfo;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static no.nav.sbl.dialogarena.redis.CacheConstants.KOMMUNEINFO_CACHE_KEY;
import static no.nav.sbl.dialogarena.redis.RedisUtils.toKommuneInfoMap;
import static no.nav.sbl.dialogarena.redis.RedisUtils.objectMapper;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class RedisServiceImpl implements RedisService {

    private static final Logger log = getLogger(RedisServiceImpl.class);

    private final RedisStore redisStore;

    @Inject
    public RedisServiceImpl(RedisStore redisStore) {
        this.redisStore = redisStore;
    }

    @Override
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

    @Override
    public String getString(String key) {
        byte[] value = redisStore.get(key);

        if (value == null) {
            return null;
        }
        return new String(value, StandardCharsets.UTF_8);
    }

    @Override
    public Map<String, KommuneInfo> getKommuneInfos() {
        byte[] value = redisStore.get(KOMMUNEINFO_CACHE_KEY);
        return toKommuneInfoMap(value);
    }

    @Override
    public void setex(String key, byte[] value, long timeToLiveSeconds) {
        String result = redisStore.setex(key, value, timeToLiveSeconds);
        handleResponse(key, result);
    }

    @Override
    public void set(String key, byte[] value) {
        String result = redisStore.set(key, value);
        handleResponse(key, result);
    }

    private void handleResponse(String key, String result) {
        if (result != null && result.equalsIgnoreCase("OK")) {
            log.debug("Redis put OK, key={}", key);
        } else {
            log.warn("Redis put feilet eller fikk timeout, key={}", key);
        }
    }
}
