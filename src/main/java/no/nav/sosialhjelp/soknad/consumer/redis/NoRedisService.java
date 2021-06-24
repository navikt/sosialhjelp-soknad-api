package no.nav.sosialhjelp.soknad.consumer.redis;


import no.nav.sosialhjelp.api.fiks.KommuneInfo;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NoRedisService implements RedisService {

    @SuppressWarnings("rawtypes")
    @Override
    public Object get(String key, Class requestedClass) {
        return null;
    }

    @Override
    public String getString(String key) {
        return null;
    }

    @Override
    public Map<String, KommuneInfo> getKommuneInfos() {
        return null;
    }

    @Override
    public void setex(String key, byte[] value, long timeToLiveSeconds) {

    }

    @Override
    public void set(String key, byte[] value) {

    }
}
