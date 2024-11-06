package no.nav.sosialhjelp.soknad.innsending.digisosapi

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

@Configuration
class MaskinportenRedisConfig {
    @Bean
    fun redisTemplate(factory: RedisConnectionFactory) =
        RedisTemplate<MaskinportenTokenCache.CacheKey, MaskinportenToken>()
            .apply { connectionFactory = factory }
}
