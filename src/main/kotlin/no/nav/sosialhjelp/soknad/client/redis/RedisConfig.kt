package no.nav.sosialhjelp.soknad.client.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.Duration

@Configuration
@Profile("!no-redis")
open class RedisConfig(
    @Value("\${redis_host}") private val host: String,
    @Value("\${redis_port}") private val port: Int,
    @Value("\${redis_password}") private val password: String
) {

    @Bean
    open fun redisClient(): RedisClient {
        val redisURI = RedisURI.builder()
            .withHost(host)
            .withPort(port)
            .withPassword(password.toCharArray())
            .withTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .build()
        return RedisClient.create(redisURI)
    }

    @Bean
    open fun redisStore(redisClient: RedisClient): RedisStore {
        return RedisStore(redisClient)
    }

    @Bean
    open fun redisService(redisStore: RedisStore): RedisService {
        log.info("Starter RedisService")
        return RedisServiceImpl(redisStore)
    }

    companion object {
        private val log = LoggerFactory.getLogger(RedisConfig::class.java)
        private const val TIMEOUT_SECONDS: Long = 1
    }
}

@Configuration
@Profile("no-redis")
open class NoRedisConfig {

    @Bean
    open fun redisService(): RedisService {
        log.error("Starter NoRedisService. Skal ikke skje i prod.")
        return NoRedisService()
    }

    companion object {
        private val log = LoggerFactory.getLogger(NoRedisConfig::class.java)
    }
}
