package no.nav.sosialhjelp.soknad.consumer.redis;

import no.nav.sosialhjelp.api.fiks.KommuneInfo;

import java.util.Map;

public interface RedisService {

    Object get(String key, Class requestedClass);

    String getString(String key);

    Map<String, KommuneInfo> getKommuneInfos();

    void setex(String key, byte[] value, long timeToLiveSeconds);

    void set(String key, byte[] value);
}
