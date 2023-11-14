package no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.app.mapper.KommuneTilNavEnhetMapper
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.MANGLER_KONFIGURASJON
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER
import org.slf4j.LoggerFactory.getLogger
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class KommuneInfoService(
    private val kommuneInfoClient: KommuneInfoClient,
) {

    @Cacheable(value = ["kommuneinfo-kanMottaSoknader"], key = "#kommunenummer")
    fun kanMottaSoknader(kommunenummer: String): Boolean {
        return getKommune(kommunenummer)?.kanMottaSoknader ?: false
    }

    @Cacheable(value = ["kommuneinfo-harMidlertidigDeaktivertMottak"], key = "#kommunenummer")
    fun harMidlertidigDeaktivertMottak(kommunenummer: String): Boolean {
        return getKommune(kommunenummer)?.harMidlertidigDeaktivertMottak ?: false
    }

    fun getBehandlingskommune(kommunenummer: String, kommunenavnFraAdresseforslag: String?): String? {
        return getKommune(kommunenummer)?.behandlingsansvarlig
            ?.let { if (it.endsWith(" kommune")) it.replace(" kommune", "") else it }
            ?: KommuneTilNavEnhetMapper.IKS_KOMMUNER.getOrDefault(kommunenummer, kommunenavnFraAdresseforslag)
    }

    fun getKommune(kommunenummer: String): KommuneInfo? = hentAlleKommuneInfo()?.get(kommunenummer)

    fun hentAlleKommuneInfo(): Map<String, KommuneInfo>? = kommuneInfoClient.getAll()?.associateBy { it.kommunenummer }

    // Det holder å sjekke om kommunen har en konfigurasjon hos fiks, har de det vil vi alltid kunne sende
    fun getKommuneStatus(kommunenummer: String, withLogging: Boolean = false): KommuneStatus {
        val kommuneInfoMap = hentAlleKommuneInfo()
        val kommuneInfo = getKommune(kommunenummer)
        if (withLogging) {
            log.info("Kommuneinfo for $kommunenummer: $kommuneInfo")
        }
        return when {
            kommuneInfoMap == null -> FIKS_NEDETID_OG_TOM_CACHE
            kommuneInfo == null -> MANGLER_KONFIGURASJON
            !kommuneInfo.kanMottaSoknader -> HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
            kommuneInfo.harMidlertidigDeaktivertMottak -> SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER
            else -> SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
        }
    }

    companion object {
        private val log = getLogger(KommuneInfoService::class.java)
    }
}
