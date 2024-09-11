package no.nav.sosialhjelp.soknad.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.Duration

@Configuration
@Profile("(!no-redis) & (!preprod)")
class RedisConfig(
    @Value("\${redis_host}:") private val host: String,
    @Value("\${redis_port}:") private val port: Int,
    @Value("\${redis_password}:") private val password: String,
) {
    private val log by logger()

    @Bean
    fun redisClient(): RedisClient {
        log.info("Lager client med gammel redis")
        val redisURI =
            RedisURI
                .builder()
                .withHost(host)
                .withPort(port)
                .withPassword(password.toCharArray())
                .withTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build()
        return RedisClient.create(redisURI)
    }

    @Bean
    fun redisStore(redisClient: RedisClient): RedisStore = RedisStore(redisClient)

    @Bean
    fun redisService(redisStore: RedisStore): RedisService {
        log.info("Starter RedisService")
        return RedisServiceImpl(redisStore)
    }

    companion object {
        private val log = LoggerFactory.getLogger(RedisConfig::class.java)
        private const val TIMEOUT_SECONDS: Long = 1
    }
}

@Configuration
@Profile("(!no-redis) & preprod")
class RedisConfigPreprod(
    @Value("\${redis_password:#{null}}") private val password: String?,
    @Value("\${redis_uri:#{null}}") private val uri: String?,
    @Value("\${redis_username:#{null}}") private val username: String?,
) {
    private val log by logger()

    @Bean
    fun redisClient(): RedisClient {
        log.info("Connecting to Redis with URI: $uri")
        return RedisClient.create(uri)
    }

    @Bean
    fun redisStore(redisClient: RedisClient): RedisStore = RedisStore(redisClient)

    @Bean
    fun redisService(redisStore: RedisStore): RedisService {
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
class NoRedisConfig {
    @Bean
    fun redisService(): RedisService {
        log.error("Starter NoRedisService. Skal ikke skje i prod.")
        return NoRedisService()
    }

    companion object {
        private val log = LoggerFactory.getLogger(NoRedisConfig::class.java)
    }
}
