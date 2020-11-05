package no.nav.sbl.dialogarena.redis;

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

import java.util.Collections;
import java.util.Map;

import static no.nav.sbl.dialogarena.redis.CacheConstants.KOMMUNEINFO_CACHE_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RedisServiceTest {

    private static final ObjectMapper objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper().registerModule(new KotlinModule());
    private final KommuneInfo kommuneInfo = new KommuneInfo("1234", true, true, true, true, null, true, null);

    @Mock
    private RedisStore redisStore;

    @InjectMocks
    private RedisService redisService;

    @Test
    public void skalHenteFraCache() throws JsonProcessingException {
        when(redisStore.get(KOMMUNEINFO_CACHE_KEY)).thenReturn(objectMapper.writeValueAsBytes(kommuneInfo));

        KommuneInfo cached = (KommuneInfo) redisService.get(KOMMUNEINFO_CACHE_KEY, KommuneInfo.class);
        assertThat(cached, is(kommuneInfo));
    }

    @Test
    public void skalHenteAlleKommuneInfos() throws JsonProcessingException {
        byte[] bytes = objectMapper.writeValueAsBytes(Collections.singletonList(kommuneInfo));
        when(redisStore.get(KOMMUNEINFO_CACHE_KEY)).thenReturn(bytes);

        Map<String, KommuneInfo> cached = redisService.getKommuneInfos();
        assertThat(cached, hasKey(kommuneInfo.getKommunenummer()));
        assertThat(cached, hasValue(kommuneInfo));
    }

    @Test
    public void ingenKommuneInfos() {
        when(redisStore.get(KOMMUNEINFO_CACHE_KEY)).thenReturn(null);

        Map<String, KommuneInfo> map = redisService.getKommuneInfos();
        assertThat(map, is(nullValue()));
    }
}