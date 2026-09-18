package no.nav.sosialhjelp.soknad.app.config

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext.fromSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.SerializationException
import org.springframework.data.redis.serializer.StringRedisSerializer
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.JavaType
import tools.jackson.module.kotlin.jacksonTypeRef
import java.time.Duration

@Configuration(proxyBeanMethods = false)
@Profile("!no-redis")
@EnableCaching
class CacheConfig : CachingConfigurer {
    @Bean
    fun cacheManager(
        redisConnectionFactory: RedisConnectionFactory,
        cacheConfigs: List<SoknadApiCacheConfig>,
    ): CacheManager =
        RedisCacheManager
            .builder(redisConnectionFactory)
            .withInitialCacheConfigurations(cacheConfigs.associate { it.cacheName to it.getConfig() })
            .enableStatistics()
            .build()

    override fun errorHandler(): CacheErrorHandler = CustomCacheErrorHandler
}

@Configuration(proxyBeanMethods = false)
@Profile("no-redis")
@EnableCaching
class CacheConfigTest : CachingConfigurer {
    @Bean
    fun cacheManager(
        cacheConfigs: List<SoknadApiCacheConfig>,
    ): CacheManager =
        ConcurrentMapCacheManager(
            *cacheConfigs
                .map { it.cacheName }
                .toTypedArray(),
        )
            .apply { isAllowNullValues = false }

    override fun errorHandler(): CacheErrorHandler = CustomCacheErrorHandler
}

object CustomCacheErrorHandler : CacheErrorHandler {
    private val log by logger()

    override fun handleCacheGetError(
        exception: RuntimeException,
        cache: Cache,
        key: Any,
    ) {
        if (exception is SerializationException) cache.evict(key)
        log.warn("Couldn't get cache value in cache ${cache.name}", exception)
    }

    override fun handleCachePutError(
        exception: RuntimeException,
        cache: Cache,
        key: Any,
        value: Any?,
    ) {
        if (exception is SerializationException) cache.evict(key)
        log.warn("Couldn't put cache value in cache ${cache.name}", exception)
    }

    override fun handleCacheEvictError(
        exception: RuntimeException,
        cache: Cache,
        key: Any,
    ) {
        log.warn("Couldn't evict cache value in cache ${cache.name}", exception)
    }

    override fun handleCacheClearError(
        exception: RuntimeException,
        cache: Cache,
    ) {
        log.warn("Couldn't clear cache ${cache.name}", exception)
    }
}

abstract class SoknadApiCacheConfig(
    val cacheName: String,
    valueType: JavaType,
    private val timeToLive: Duration = Duration.ofHours(1),
) {
    private val valueSerializationPair =
        fromSerializer<Any>(cacheValueSerializer(valueType)).valueSerializationPair

    open fun getConfig(): RedisCacheConfiguration =
        RedisCacheConfiguration
            .defaultCacheConfig()
            .disableCachingNullValues()
            .entryTtl(timeToLive)
            .serializeKeysWith(CacheDefaults.keySerializationPair)
            .serializeValuesWith(valueSerializationPair)
}

internal val cacheMapper =
    sosialhjelpJsonMapperBuilder()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build()

internal inline fun <reified T> cacheValueType(): JavaType =
    cacheMapper.typeFactory.constructType(jacksonTypeRef<T>().type)

internal fun <T : Any> cacheValueSerializer(valueType: JavaType): RedisSerializer<T> =
    JacksonJsonRedisSerializer(cacheMapper, valueType)

private object CacheDefaults {
    val keySerializationPair = fromSerializer(StringRedisSerializer()).keySerializationPair
}
