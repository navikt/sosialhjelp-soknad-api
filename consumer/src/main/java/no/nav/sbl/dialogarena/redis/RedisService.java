package no.nav.sbl.dialogarena.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper;
import no.nav.sosialhjelp.api.fiks.KommuneInfo;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.redis.CacheConstants.KOMMUNEINFO_CACHE_KEY;
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

    public static Map<String, KommuneInfo> toKommuneInfoMap(byte[] value) {
        if (value != null) {
            try {
                return Arrays
                        .stream(objectMapper.readValue(value, KommuneInfo[].class))
                        .collect(Collectors.toMap(KommuneInfo::getKommunenummer, Function.identity()));
            } catch (IOException e) {
                log.warn("noe feilet ved deserialisering til kommuneInfoMap", e);
            }
        }
        return null;
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

    public String getString(String key) {
        byte[] value = redisStore.get(key);

        if (value == null) {
            return null;
        }
        return new String(value, StandardCharsets.UTF_8);
    }

    public Map<String, KommuneInfo> getKommuneInfos() {
        byte[] value = redisStore.get(KOMMUNEINFO_CACHE_KEY);
        return toKommuneInfoMap(value);
    }

    public void setex(String key, byte[] value, long timeToLiveSeconds) {
        String set = redisStore.setex(key, value, timeToLiveSeconds);
        handleResponse(key, set);
    }

    public void set(String key, byte[] value) {
        String set = redisStore.set(key, value);
        handleResponse(key, set);
    }

    private void handleResponse(String key, String set) {
        if (set.equalsIgnoreCase("OK")) {
            log.debug("Redis put OK, key={}", key);
        } else {
            log.warn("Redis put feilet eller fikk timeout, key={}", key);
        }
    }
}
