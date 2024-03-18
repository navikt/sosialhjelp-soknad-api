package no.nav.sosialhjelp.soknad.kodeverk

import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDataService.Companion.Kommuner
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDataService.Companion.Landkoder
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDataService.Companion.Postnummer
import org.springframework.stereotype.Service

@Service
class KodeverkService(
    private val kodeverkDataService: KodeverkDataService,
) {
    fun getKommunenavn(kommunenummer: String): String? = kodeverkDataService.hentKodeverk(Kommuner)[kommunenummer]
    fun gjettKommunenummer(kommunenavn: String): String? {
        val kommuner = kodeverkDataService.hentKodeverk(Kommuner)
        return kommuner.keys.firstOrNull { key -> kommuner[key] == kommunenavn }
    }

    fun getPoststed(postnummer: String): String? = kodeverkDataService.hentKodeverk(Postnummer)[postnummer]
    fun getLand(landkode: String): String? = kodeverkDataService.hentKodeverk(Landkoder)[landkode]
}
