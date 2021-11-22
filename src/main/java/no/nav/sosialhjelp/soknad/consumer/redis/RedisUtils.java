package no.nav.sosialhjelp.soknad.consumer.redis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper;

public final class RedisUtils {

    private RedisUtils() {
    }

    public static final ObjectMapper redisObjectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .registerModule(new KotlinModule())
            .registerModule(new JavaTimeModule());
}
