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
        return doGet(personIdService.findPersonId(soknadId))
            .also { info -> if (info == null) logger.warn("Krr - response er null") }
            ?.also { info -> if (info.mobiltelefonnummer == null) logger.warn("Krr - mobiltelefonnummer er null") }
            ?.mobiltelefonnummer
    }

    private fun doGet(personId: String): DigitalKontaktinformasjon? {
        val kontaktInfoResponse = krrClient.getDigitalKontaktinformasjon(personId) ?: return null

        return kontaktInfoResponse.personer
            ?.let { infoForPersonMap -> infoForPersonMap[personId] }
            .also { info -> if (info == null) kontaktInfoResponse.logError(personId) }
    }

    private fun KontaktInfoResponse.logError(personId: String) {
        feil?.get(personId)
            ?.also { message -> logger.error("Kunne ikke hente fra KRR: $message") }
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

data class DigitalKontaktinformasjon(
    val personident: String,
    val aktiv: Boolean,
    val kanVarsles: Boolean?,
    val reservert: Boolean?,
    val mobiltelefonnummer: String?,
)
