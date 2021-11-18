package no.nav.sosialhjelp.soknad.consumer.pdl.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;

public final class Utils {

    private Utils() {
    }

    public static final ObjectMapper pdlMapper = JsonMapper.builder()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .addModule(new JavaTimeModule())
            .addModule(new KotlinModule())
            .build();
}
