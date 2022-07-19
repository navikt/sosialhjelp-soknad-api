package no.nav.sosialhjelp.soknad.redis

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.redis.RedisUtils.redisObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

internal class RedisServiceImplTest {

    private val redisStore = mockk<RedisStore>()
    private val redisService = RedisServiceImpl(redisStore)

    private val kommuneInfo = KommuneInfo("1234", true, true, true, true, null, true, null)

    @Test
    fun skalHenteFraCache() {
        every { redisStore.get(KOMMUNEINFO_CACHE_KEY) } returns redisObjectMapper.writeValueAsBytes(kommuneInfo)
        val cached = redisService.get(KOMMUNEINFO_CACHE_KEY, KommuneInfo::class.java) as KommuneInfo?
        assertThat(cached).isEqualTo(kommuneInfo)
    }

    @Test
    fun skalHenteAlleKommuneInfos() {
        val bytes = redisObjectMapper.writeValueAsBytes(listOf(kommuneInfo))
        every { redisStore.get(KOMMUNEINFO_CACHE_KEY) } returns bytes
        val cached = redisService.getKommuneInfos()
        assertThat(cached).containsKey(kommuneInfo.kommunenummer)
        assertThat(cached).containsValue(kommuneInfo)
    }

    @Test
    fun ingenKommuneInfos() {
        every { redisStore.get(KOMMUNEINFO_CACHE_KEY) } returns null
        val map = redisService.getKommuneInfos()
        assertThat(map).isNull()
    }

    @Test
    fun skalHandtereNullFraRedisStore() {
        val key = "key"
        val value = "value".toByteArray(StandardCharsets.UTF_8)
        every { redisStore.set(key, value) } returns null
        redisService.set("key", "value".toByteArray(StandardCharsets.UTF_8))
    }
}
