package no.nav.sosialhjelp.soknad.innsending.digisosapi

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class MaskinportenTokenCache(
    private val redisTemplate: RedisTemplate<CacheKey, MaskinportenToken>,
    meterRegistry: MeterRegistry,
) {
    private var hitCounter = meterRegistry.counter("maskinporten.cache.hit.count")
    private var missCounter = meterRegistry.counter("maskinporten.cache.miss.count")

    fun put(
        pid: String,
        scope: String,
        token: MaskinportenToken,
    ) = redisTemplate.opsForValue().set(CacheKey(pid, scope), token, token.expires_in, TimeUnit.SECONDS)

    fun get(
        pid: String,
        scope: String,
    ): String? =
        redisTemplate.opsForValue().get(CacheKey(pid, scope))?.access_token
            ?.also { hitCounter.increment() } ?: null.also { missCounter.increment() }

    data class CacheKey(val pid: String, val scope: String)
}
