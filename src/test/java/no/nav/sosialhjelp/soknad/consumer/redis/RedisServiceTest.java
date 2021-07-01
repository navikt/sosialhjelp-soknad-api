package no.nav.sosialhjelp.soknad.consumer.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper;
import no.nav.sosialhjelp.api.fiks.KommuneInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.KOMMUNEINFO_CACHE_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RedisServiceTest {

    private static final ObjectMapper objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper().registerModule(new KotlinModule());
    private final KommuneInfo kommuneInfo = new KommuneInfo("1234", true, true, true, true, null, true, null);

    @Mock
    private RedisStore redisStore;

    @InjectMocks
    private RedisServiceImpl redisService;

    @Test
    public void skalHenteFraCache() throws JsonProcessingException {
        when(redisStore.get(KOMMUNEINFO_CACHE_KEY)).thenReturn(objectMapper.writeValueAsBytes(kommuneInfo));

        KommuneInfo cached = (KommuneInfo) redisService.get(KOMMUNEINFO_CACHE_KEY, KommuneInfo.class);
        assertThat(cached).isEqualTo(kommuneInfo);
    }

    @Test
    public void skalHenteAlleKommuneInfos() throws JsonProcessingException {
        byte[] bytes = objectMapper.writeValueAsBytes(Collections.singletonList(kommuneInfo));
        when(redisStore.get(KOMMUNEINFO_CACHE_KEY)).thenReturn(bytes);

        Map<String, KommuneInfo> cached = redisService.getKommuneInfos();
        assertThat(cached).containsKey(kommuneInfo.getKommunenummer());
        assertThat(cached).containsValue(kommuneInfo);
    }

    @Test
    public void ingenKommuneInfos() {
        when(redisStore.get(KOMMUNEINFO_CACHE_KEY)).thenReturn(null);

        Map<String, KommuneInfo> map = redisService.getKommuneInfos();
        assertThat(map).isNull();
    }

    @Test
    public void skalHandtereNullFraRedisStore() {
        var key = "key";
        var value = "value".getBytes(StandardCharsets.UTF_8);

        when(redisStore.set(key, value)).thenReturn(null);

        redisService.set("key", "value".getBytes(StandardCharsets.UTF_8));
    }
}