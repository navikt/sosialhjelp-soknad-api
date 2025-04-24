package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.config.SoknadApiCacheConfig
import no.nav.sosialhjelp.soknad.v2.soknad.PersonIdService
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

@Component
class KrrService(
    private val krrClient: KrrClient,
    private val personIdService: PersonIdService,
) {
    @Cacheable(KrrCacheConfig.CACHE_NAME)
    fun getMobilnummer(soknadId: UUID): String? {
        logger.info("Henter digital kontaktinformasjon")
        return personIdService.findPersonId(soknadId)
            .let { personId -> krrClient.getDigitalKontaktinformasjon(personId) }
            .also { info -> if (info == null) logger.warn("Krr - response er null") }
            ?.also { info -> if (info.mobiltelefonnummer == null) logger.warn("Krr - mobiltelefonnummer er null") }
            ?.mobiltelefonnummer
    }

    companion object {
        private val logger by logger()
    }
}

@Configuration
class KrrCacheConfig : SoknadApiCacheConfig(CACHE_NAME) {
    override fun getConfig(): RedisCacheConfiguration =
        RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(EIGHT_HOURS)

    companion object {
        const val CACHE_NAME = "krr-cache"
        private val EIGHT_HOURS = Duration.ofHours(8)
    }
}
