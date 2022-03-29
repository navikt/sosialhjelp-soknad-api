package no.nav.sosialhjelp.soknad.navenhet

import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.client.redis.GT_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.client.redis.GT_LAST_POLL_TIME_PREFIX
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import no.nav.sosialhjelp.soknad.client.redis.RedisUtils.redisObjectMapper
import no.nav.sosialhjelp.soknad.navenhet.domain.NavEnhet
import no.nav.sosialhjelp.soknad.navenhet.domain.NavEnhetFraLokalListe
import no.nav.sosialhjelp.soknad.navenhet.domain.NavEnheterFraLokalListe
import no.nav.sosialhjelp.soknad.navenhet.domain.toNavEnhet
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetDto
import no.nav.sosialhjelp.soknad.navenhet.dto.toNavEnhet
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface NavEnhetService {
    fun getEnhetForGt(gt: String?): NavEnhet?
    fun getEnheterForKommunenummer(kommunenummer: String?): List<NavEnhet>?
}

@Component
class NavEnhetServiceImpl(
    private val norgClient: NorgClient,
    private val redisService: RedisService
) : NavEnhetService {

    private var cachedNavenheterFraLokalListe: List<NavEnhetFraLokalListe>? = null

    override fun getEnhetForGt(gt: String?): NavEnhet? {
        if (gt == null || !gt.matches(Regex("^[0-9]+$"))) {
            throw IllegalArgumentException("GT ikke på gyldig format: $gt")
        }

        val navEnhetDto = hentFraCacheEllerConsumer(gt)
        if (navEnhetDto == null) {
            log.warn("Kunne ikke finne NorgEnhet for gt: $gt")
            return null
        }
        return navEnhetDto.toNavEnhet(gt)
    }

    override fun getEnheterForKommunenummer(kommunenummer: String?): List<NavEnhet>? {
        return getNavenhetForKommunenummerFraCacheEllerLokalListe(kommunenummer)
            ?.map { it.toNavEnhet() }
            ?.distinct()
    }

    private fun hentFraCacheEllerConsumer(gt: String): NavEnhetDto? {
        if (skalBrukeCache(gt)) {
            val cached = hentFraCache(gt)
            if (cached != null) {
                return cached
            }
        }
        return hentFraConsumerMedCacheSomFallback(gt)
    }

    private fun skalBrukeCache(gt: String): Boolean {
        val timeString = redisService.getString(GT_LAST_POLL_TIME_PREFIX + gt) ?: return false
        val lastPollTime = LocalDateTime.parse(timeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        return lastPollTime.plusMinutes(MINUTES_TO_PASS_BETWEEN_POLL).isAfter(LocalDateTime.now())
    }

    private fun hentFraCache(gt: String): NavEnhetDto? {
        return redisService.get(GT_CACHE_KEY_PREFIX + gt, NavEnhetDto::class.java) as? NavEnhetDto
    }

    private fun hentFraConsumerMedCacheSomFallback(gt: String): NavEnhetDto? {
        try {
            val navEnhetDto = norgClient.hentNavEnhetForGeografiskTilknytning(gt)
            if (navEnhetDto != null) {
                return navEnhetDto
            }
        } catch (e: TjenesteUtilgjengeligException) {
            // Norg feiler -> prøv å hent tidligere cached verdi
            val cached = hentFraCache(gt)
            if (cached != null) {
                log.info("Norg-client feilet, men bruker tidligere cachet response fra Norg")
                return cached
            }
            log.warn("Norg-client feilet og cache for gt=$gt er tom")
            throw e
        }
        return null
    }

    private fun getNavenhetForKommunenummerFraCacheEllerLokalListe(kommunenummer: String?): List<NavEnhetFraLokalListe>? {
        if (cachedNavenheterFraLokalListe == null) {
            val (navEnheter) = getAllNavenheterFromPath() ?: throw IllegalStateException("Fant ingen navenheter i path: $NAVENHET_PATH")
            cachedNavenheterFraLokalListe = navEnheter
        }
        return cachedNavenheterFraLokalListe
            ?.filter { navenhet -> navenhet.kommunenummer == kommunenummer }
            ?.distinct()
    }

    private fun getAllNavenheterFromPath(): NavEnheterFraLokalListe? {
        return try {
            val resourceAsStream = this.javaClass.getResourceAsStream(NAVENHET_PATH) ?: return null
            val json = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8)
            redisObjectMapper.readValue(json, NavEnheterFraLokalListe::class.java)
        } catch (e: IOException) {
            log.error("IOException ved henting av navenheter fra lokal liste", e)
            null
        }
    }

    companion object {
        private val log = getLogger(NavEnhetService::class.java)

        private const val NAVENHET_PATH = "/navenhet.json"
        private const val MINUTES_TO_PASS_BETWEEN_POLL: Long = 60
    }
}
