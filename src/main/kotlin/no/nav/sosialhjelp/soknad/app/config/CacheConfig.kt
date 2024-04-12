package no.nav.sosialhjelp.soknad.app.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    fun cacheManager(
        redisConnectionFactory: RedisConnectionFactory,
        @Value("\${digisos.cache.kodeverk.time-to-live}") kodeverkTTL: Long,
        @Value("\${digisos.cache.pdl.time-to-live}") pdlTTL: Long
    ): CacheManager = RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig())
        .withInitialCacheConfigurations(
            mapOf(
                "kodeverk" to RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(kodeverkTTL)),
                "pdl" to RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(pdlTTL))
            )
        )
        .enableStatistics()
        .build()
}
