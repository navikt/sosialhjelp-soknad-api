package no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo

import com.fasterxml.jackson.core.JsonProcessingException
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.mapper.KommuneTilNavEnhetMapper
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.MANGLER_KONFIGURASJON
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER
import no.nav.sosialhjelp.soknad.redis.KOMMUNEINFO_CACHE_KEY
import no.nav.sosialhjelp.soknad.redis.KOMMUNEINFO_CACHE_SECONDS
import no.nav.sosialhjelp.soknad.redis.KOMMUNEINFO_LAST_POLL_TIME_KEY
import no.nav.sosialhjelp.soknad.redis.RedisService
import no.nav.sosialhjelp.soknad.redis.RedisUtils.redisObjectMapper
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

@Component
class KommuneInfoService(
    private val kommuneInfoClient: KommuneInfoClient,
    private val redisService: RedisService,
) {

    fun kanMottaSoknader(kommunenummer: String): Boolean {
        return hentAlleKommuneInfo()
            ?.getOrDefault(kommunenummer, DEFAULT_KOMMUNEINFO)?.kanMottaSoknader
            ?: return false
    }

    fun harMidlertidigDeaktivertMottak(kommunenummer: String): Boolean {
        return hentAlleKommuneInfo()
            ?.getOrDefault(kommunenummer, DEFAULT_KOMMUNEINFO)?.harMidlertidigDeaktivertMottak
            ?: return false
    }

    fun hentAlleKommuneInfo(): Map<String, KommuneInfo>? {
        if (skalBrukeCache()) {
            val cachedMap = redisService.getKommuneInfos()
            if (!cachedMap.isNullOrEmpty()) {
                return cachedMap
            }
            log.info("hentAlleKommuneInfo - cache er tom.")
        }
        val kommuneInfoList = hentKommuneInfoFraFiks()
        oppdaterCache(kommuneInfoList)

        val kommuneInfoMap = kommuneInfoList.associateBy { it.kommunenummer }

        if (kommuneInfoMap.isEmpty()) {
            val cachedMap = redisService.getKommuneInfos()
            if (!cachedMap.isNullOrEmpty()) {
                log.info("hentAlleKommuneInfo - feiler mot Fiks. Bruker cache mens Fiks er nede.")
                return cachedMap
            }
            log.error("hentAlleKommuneInfo - feiler mot Fiks og cache er tom.")
            return null
        }
        return kommuneInfoMap
    }

    private fun skalBrukeCache(): Boolean {
        return redisService.getString(KOMMUNEINFO_LAST_POLL_TIME_KEY)
            ?.let { LocalDateTime.parse(it, ISO_LOCAL_DATE_TIME).plusMinutes(MINUTES_TO_PASS_BETWEEN_POLL).isAfter(LocalDateTime.now()) }
            ?: false
    }

    fun getBehandlingskommune(kommunenr: String, kommunenavnFraAdresseforslag: String?): String? {
        return behandlingsansvarlig(kommunenr)
            ?.let { if (it.endsWith(" kommune")) it.replace(" kommune", "") else it }
            ?: KommuneTilNavEnhetMapper.IKS_KOMMUNER.getOrDefault(kommunenr, kommunenavnFraAdresseforslag)
    }

    // Det holder å sjekke om kommunen har en konfigurasjon hos fiks, har de det vil vi alltid kunne sende
    fun kommuneInfo(kommunenummer: String): KommuneStatus {
        val kommuneInfoMap = hentAlleKommuneInfo()
        val kommuneInfo = kommuneInfoMap?.getOrDefault(kommunenummer, null)
        log.info("Kommuneinfo for $kommunenummer: $kommuneInfo")
        return when {
            kommuneInfoMap == null -> FIKS_NEDETID_OG_TOM_CACHE
            kommuneInfo == null -> MANGLER_KONFIGURASJON
            !kommuneInfo.kanMottaSoknader -> HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
            kommuneInfo.harMidlertidigDeaktivertMottak -> SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER
            else -> SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
        }
    }

    fun getKommuneStatus(kommunenummer: String): KommuneStatus {
        val kommuneInfoMap = hentAlleKommuneInfo()
        val kommuneInfo = kommuneInfoMap?.getOrDefault(kommunenummer, null)
        return when {
            kommuneInfoMap == null -> FIKS_NEDETID_OG_TOM_CACHE
            kommuneInfo == null -> MANGLER_KONFIGURASJON
            !kommuneInfo.kanMottaSoknader -> HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
            kommuneInfo.harMidlertidigDeaktivertMottak -> SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER
            else -> SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
        }
    }

    private fun behandlingsansvarlig(kommunenummer: String): String? {
        return hentAlleKommuneInfo()?.getOrDefault(kommunenummer, DEFAULT_KOMMUNEINFO)?.behandlingsansvarlig
    }

    private fun oppdaterCache(kommuneInfoList: List<KommuneInfo>?) {
        try {
            if (!kommuneInfoList.isNullOrEmpty()) {
                redisService.setex(
                    KOMMUNEINFO_CACHE_KEY,
                    redisObjectMapper.writeValueAsBytes(kommuneInfoList),
                    KOMMUNEINFO_CACHE_SECONDS
                )
                redisService.set(
                    KOMMUNEINFO_LAST_POLL_TIME_KEY,
                    LocalDateTime.now().format(ISO_LOCAL_DATE_TIME).toByteArray(UTF_8)
                )
            }
        } catch (e: JsonProcessingException) {
            log.warn("Noe galt skjedde ved mapping av kommuneinfolist for caching i redis", e)
        }
    }

    private fun hentKommuneInfoFraFiks(): List<KommuneInfo> {
        return kommuneInfoClient.getAll()
            .also { log.info("Hentet kommuneinfo ved bruk av maskinporten-integrasjon mot ks:fiks") }
    }

    companion object {
        private val log by logger()
        private const val MINUTES_TO_PASS_BETWEEN_POLL: Long = 10

        private val DEFAULT_KOMMUNEINFO = KommuneInfo(
            kommunenummer = "",
            kanMottaSoknader = false,
            kanOppdatereStatus = false,
            harMidlertidigDeaktivertMottak = false,
            harMidlertidigDeaktivertOppdateringer = false,
            kontaktpersoner = null,
            harNksTilgang = false,
            behandlingsansvarlig = null
        )
    }
}
