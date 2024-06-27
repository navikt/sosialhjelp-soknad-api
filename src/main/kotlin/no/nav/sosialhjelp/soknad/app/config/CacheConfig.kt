package no.nav.sosialhjelp.soknad.app.config

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {
    private val objectMapper = jacksonObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())

    @Bean
    @Primary
    fun defaultCacheConfig(): RedisCacheConfiguration =
        RedisCacheConfiguration
            .defaultCacheConfig()
            .serializeKeysWith(SerializationPair.fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer(objectMapper)))

    @Bean
    fun cacheManager(
        redisConnectionFactory: RedisConnectionFactory,
        cacheConfig: RedisCacheConfiguration,
        @Value("\${digisos.cache.kodeverk.time-to-live}") kodeverkTTL: Long,
        @Value("\${digisos.cache.digisos_soker.time-to-live}") digisosSokerTTL: Long,
        @Value("\${digisos.cache.digisos_sak.time-to-live}") digisosSakTTL: Long,
    ): CacheManager =
        RedisCacheManager
            .builder(redisConnectionFactory)
            .cacheDefaults(
                cacheConfig,
            ).withInitialCacheConfigurations(
                mapOf(
                    "kodeverk" to cacheConfig.entryTtl(Duration.ofSeconds(kodeverkTTL)),
                    "digisos_sak" to cacheConfig.entryTtl(Duration.ofSeconds(digisosSakTTL)),
                    "digisos_soker" to cacheConfig.entryTtl(Duration.ofSeconds(digisosSokerTTL)),
                ),
            ).enableStatistics()
            .build()
}
