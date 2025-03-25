package no.nav.sosialhjelp.soknad.app.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.time.Duration

@Configuration(proxyBeanMethods = false)
@EnableCaching
class CacheConfig {
    @Bean
    fun valkeyCustomizer(
        @Value("\${digisos.cache.kodeverk.time-to-live}") kodeverkTTL: Long,
    ): RedisCacheManagerBuilderCustomizer {
        return RedisCacheManagerBuilderCustomizer { builder ->
            builder
                .withCacheConfiguration(
                    "kodeverk",
                    RedisCacheConfiguration
                        .defaultCacheConfig().entryTtl(Duration.ofSeconds(kodeverkTTL)),
                )
        }
    }

    @Bean
    fun cacheManager(
        redisConnectionFactory: RedisConnectionFactory,
        @Value("\${digisos.cache.kodeverk.time-to-live}") kodeverkTTL: Long,
    ): CacheManager =
        RedisCacheManager
            .builder(redisConnectionFactory)
            .cacheDefaults(
                RedisCacheConfiguration.defaultCacheConfig(),
            ).withInitialCacheConfigurations(
                mapOf(
                    "kodeverk" to RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(kodeverkTTL)),
                ),
            ).enableStatistics()
            .build()
}
