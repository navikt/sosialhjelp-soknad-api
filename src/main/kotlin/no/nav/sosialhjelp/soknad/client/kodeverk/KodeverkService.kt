package no.nav.sosialhjelp.soknad.client.kodeverk

import no.nav.sosialhjelp.soknad.client.kodeverk.dto.KodeverkDto
import no.nav.sosialhjelp.soknad.client.redis.KODEVERK_LAST_POLL_TIME_KEY
import no.nav.sosialhjelp.soknad.client.redis.KOMMUNER_CACHE_KEY
import no.nav.sosialhjelp.soknad.client.redis.LANDKODER_CACHE_KEY
import no.nav.sosialhjelp.soknad.client.redis.POSTNUMMER_CACHE_KEY
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

open class KodeverkService(
    private val kodeverkClient: KodeverkClient,
    private val redisService: RedisService
) {

    open fun getKommunenavn(kommunenummer: String): String? {
        val kommuneKodeverk = hentKodeverkFraCacheEllerConsumer(KOMMUNER_CACHE_KEY)
        return finnFoersteTermForKodeverdi(kommuneKodeverk, kommunenummer)
    }

    open fun gjettKommunenummer(kommunenavn: String): String? {
        val kommuneKodeverk = hentKodeverkFraCacheEllerConsumer(KOMMUNER_CACHE_KEY)
        return finnKodeverdiForFoersteTerm(kommuneKodeverk, kommunenavn)
    }

    open fun getPoststed(postnummer: String): String? {
        val postnummerKodeverk = hentKodeverkFraCacheEllerConsumer(POSTNUMMER_CACHE_KEY)
        return finnFoersteTermForKodeverdi(postnummerKodeverk, postnummer)
    }

    open fun getLand(landkode: String): String? {
        val landkoderKodeverk = hentKodeverkFraCacheEllerConsumer(LANDKODER_CACHE_KEY)
        val land = finnFoersteTermForKodeverdi(landkoderKodeverk, landkode)
        return formaterLand(land)
    }

    private fun hentKodeverkFraCacheEllerConsumer(key: String): KodeverkDto? {
        if (skalBrukeCache()) {
            val kodeverk = hentFraCache(key)
            if (kodeverk != null) {
                return kodeverk
            }
        }
        when (key) {
            KOMMUNER_CACHE_KEY -> return kodeverkFraClientEllerCacheSomFallback(key, kodeverkClient.hentKommuner())
            POSTNUMMER_CACHE_KEY -> return kodeverkFraClientEllerCacheSomFallback(key, kodeverkClient.hentPostnummer())
            LANDKODER_CACHE_KEY -> return kodeverkFraClientEllerCacheSomFallback(key, kodeverkClient.hentLandkoder())
        }
        return null
    }

    private fun skalBrukeCache(): Boolean {
        return redisService.getString(KODEVERK_LAST_POLL_TIME_KEY)
            ?.let {
                val lastPollTime = LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                lastPollTime.plusMinutes(MINUTES_TO_PASS_BETWEEN_POLL).isAfter(LocalDateTime.now())
            } ?: false
    }

    private fun hentFraCache(key: String): KodeverkDto? {
        return redisService.get(key, KodeverkDto::class.java) as? KodeverkDto
    }

    private fun kodeverkFraClientEllerCacheSomFallback(key: String, kodeverk: KodeverkDto?): KodeverkDto? {
        if (kodeverk != null) {
            return kodeverk
        }
        val cachedKodeverk = hentFraCache(key)
        if (cachedKodeverk != null) {
            log.info("Kodeverk-client feilet, men bruker cached kodeverk.")
            return cachedKodeverk
        }
        log.warn("Kodeverk feiler og cache [$key] er tom")
        return null
    }

    private fun finnKodeverdiForFoersteTerm(kodeverk: KodeverkDto?, term: String): String? {
        if (kodeverk == null) {
            return null
        }

        return kodeverk.betydninger?.entries
            ?.firstOrNull {
                val dtos = it.value
                if (dtos.isNotEmpty() && dtos[0].beskrivelser?.get(SPRAAKKODE_NB)?.term != null) {
                    term.equals(dtos[0].beskrivelser?.get(SPRAAKKODE_NB)?.term, ignoreCase = true)
                } else false
            }
            ?.key
    }

    private fun finnFoersteTermForKodeverdi(kodeverk: KodeverkDto?, kodeverdi: String): String? {
        return if (kodeverk != null && kodeverk.betydninger?.containsKey(kodeverdi) == true) {
            kodeverk.betydninger[kodeverdi]?.get(0)?.beskrivelser?.get(SPRAAKKODE_NB)?.term
        } else null
    }

    private fun formaterLand(land: String?): String? {
        if (land == null) {
            return land
        }
        val formaterMedSpace = setUpperCaseBeforeRegex(land.lowercase(Locale.getDefault()), " ")
        val formaterMedDash = setUpperCaseBeforeRegex(formaterMedSpace, "-")
        return setUpperCaseBeforeRegex(formaterMedDash, "/")
    }

    private fun setUpperCaseBeforeRegex(s: String, regex: String): String {
        val split = s.split(regex.toRegex()).toTypedArray()
        val sb = StringBuilder()
        for (i in split.indices) {
            if (i > 0) {
                sb.append(regex)
            }
            if (split[i] == "og") {
                sb.append(split[i])
            } else {
                sb.append(split[i].substring(0, 1).uppercase(Locale.getDefault()))
                sb.append(split[i].substring(1))
            }
        }
        return sb.toString()
    }

    companion object {
        private val log = LoggerFactory.getLogger(KodeverkService::class.java)
        private const val MINUTES_TO_PASS_BETWEEN_POLL: Long = 60

        const val SPRAAKKODE_NB = "nb"
    }
}
