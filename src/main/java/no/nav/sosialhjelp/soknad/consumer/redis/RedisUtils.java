package no.nav.sosialhjelp.soknad.consumer.redis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper;
import no.nav.sosialhjelp.api.fiks.KommuneInfo;
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations.MDC_BEHANDLINGS_ID;
import static org.slf4j.LoggerFactory.getLogger;

public final class RedisUtils {

    private RedisUtils() {
    }

    private static final Logger log = getLogger(RedisUtils.class);
    public static final ObjectMapper objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .registerModule(new KotlinModule())
            .registerModule(new JavaTimeModule());

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

    public static String cacheKey(CacheType type, String ident) {
        final var behandlingsId = Optional.ofNullable(MDCOperations.getFromMDC(MDC_BEHANDLINGS_ID));
        if (behandlingsId.isPresent()) {
            return type.getPrefix() + behandlingsId.get();
        }
        log.info("behandlingsId ikke satt i MDC");
        return type.getPrefix() + ident;
    }

}
