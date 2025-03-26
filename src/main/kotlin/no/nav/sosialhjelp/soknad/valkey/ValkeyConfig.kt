package no.nav.sosialhjelp.soknad.valkey

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.Duration

// TODO: Migrer til å bruke Valkey på ordentlig. Vi kommer ikke til å kunne bruke nye valkey-features før dette er gjort
//   Vi bruker valkey, men behandler den som en redis-instans (bruker ikke valkey-features).
@Configuration
@Profile("!no-redis")
class ValkeyConfig(
    @Value("\${redis_host}") private val host: String,
    @Value("\${redis_port}") private val port: Int,
    @Value("\${redis_password}") private val password: String,
    @Value("\${redis_username}") private val username: String,
) {
    private val log by logger()

    @Bean
    fun valkeyClientGcp(): RedisClient {
        val redisURI =
            RedisURI
                .builder()
                .withHost(host)
                .withPort(port)
                .withAuthentication(username, password.toCharArray())
                .withTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .withSsl(true)
                .build()
        return RedisClient.create(redisURI)
    }

    // TODO: Erstattes med spring-boots @Cacheable
    @Bean
    fun valkeyStore(redisClient: RedisClient): ValkeyStore = ValkeyStore(redisClient)

    // TODO: Erstattes med spring-boots @Cacheable
    @Bean
    fun valkeyService(valkeyStore: ValkeyStore): ValkeyService {
        log.info("Starter RedisService")
        return ValkeyServiceImpl(valkeyStore)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ValkeyConfig::class.java)
        private const val TIMEOUT_SECONDS: Long = 1
    }
}

@Configuration
@Profile("no-redis")
class NoValkeyConfig {
    @Bean
    fun valkeyService(): ValkeyService {
        log.error("Starter NoValkeyService. Skal ikke skje i prod.")
        return NoValkeyService()
    }

    companion object {
        private val log = LoggerFactory.getLogger(NoValkeyConfig::class.java)
    }
}
