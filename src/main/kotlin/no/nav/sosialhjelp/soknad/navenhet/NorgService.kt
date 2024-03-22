package no.nav.sosialhjelp.soknad.navenhet

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.navenhet.domain.NavEnhet
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetDto
import no.nav.sosialhjelp.soknad.navenhet.dto.toNavEnhet
import no.nav.sosialhjelp.soknad.redis.GT_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.soknad.redis.GT_LAST_POLL_TIME_PREFIX
import no.nav.sosialhjelp.soknad.redis.RedisService
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class NorgService(
    private val norgClient: NorgClient,
    private val redisService: RedisService
) {

    fun getEnhetForGt(gt: String?): NavEnhet? {
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

    private fun hentFraCacheEllerConsumer(gt: String): NavEnhetDto? {
        if (skalBrukeCache(gt)) {
            val cached = hentFraCache(gt)
            if (cached != null) {
                // TODO Ekstra logging
                log.info("Bruker norg-enhet fra cache: $cached")
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
                // TODO Ekstra logging
                log.info("Bruker NavEnhet fra Norg: $navEnhetDto")
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

    companion object {
        private val log by logger()

        private const val MINUTES_TO_PASS_BETWEEN_POLL: Long = 60
    }
}
