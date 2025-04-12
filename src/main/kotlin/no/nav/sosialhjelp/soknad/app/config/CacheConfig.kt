package no.nav.sosialhjelp.soknad.app.config

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.lang.RuntimeException
import java.time.Duration

@Configuration(proxyBeanMethods = false)
@EnableCaching
class CacheConfig : CachingConfigurer {
    @Bean
    fun valkeyCustomizer(
        @Value("\${digisos.cache.kodeverk.time-to-live}") kodeverkTTL: Long,
    ): RedisCacheManagerBuilderCustomizer {
        return RedisCacheManagerBuilderCustomizer { builder ->
            builder.withCacheConfiguration(
                "kodeverk",
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(kodeverkTTL)),
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
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig())
            .withInitialCacheConfigurations(
                mapOf(
                    "kodeverk" to RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(kodeverkTTL)),
                ),
            )
            .enableStatistics()
            .build()

    override fun errorHandler(): CacheErrorHandler = CustomCacheErrorHandler()
}

class CustomCacheErrorHandler : CacheErrorHandler {
    private val log by logger()

    override fun handleCacheGetError(
        exception: RuntimeException,
        cache: Cache,
        key: Any,
    ) {
        log.warn("Couldn't get cache value for key $key in cache ${cache.name}", exception)
    }

    override fun handleCachePutError(
        exception: RuntimeException,
        cache: Cache,
        key: Any,
        value: Any?,
    ) {
        log.warn("Couldn't put cache value for key $key in cache ${cache.name}", exception)
    }

    override fun handleCacheEvictError(
        exception: RuntimeException,
        cache: Cache,
        key: Any,
    ) {
        log.warn("Couldn't evict cache value for key $key in cache ${cache.name}", exception)
    }

    override fun handleCacheClearError(
        exception: RuntimeException,
        cache: Cache,
    ) {
        log.warn("Couldn't clear cache ${cache.name}", exception)
    }
}
