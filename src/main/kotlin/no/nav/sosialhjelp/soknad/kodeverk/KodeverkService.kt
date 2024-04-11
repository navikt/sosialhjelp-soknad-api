package no.nav.sosialhjelp.soknad.kodeverk

import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDataService.Companion.Kommuner
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDataService.Companion.Landkoder
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDataService.Companion.Postnummer
import org.springframework.stereotype.Service

/**
 * På sikt bør også grensesnittet til KodeverkService revideres så vi ikke bare returnerer null dersom noe går galt her - vi vil jo helst vite om det!
 *
 */
@Service
class KodeverkService(
    private val kodeverkDataService: KodeverkDataService,
) {
    fun getKommunenavn(kommunenummer: String): String? =
        runCatching { kodeverkDataService.hentKodeverk(Kommuner)[kommunenummer] }.getOrNull()

    fun gjettKommunenummer(kommunenavn: String): String? =
        runCatching {
            val kommuner = kodeverkDataService.hentKodeverk(Kommuner)
            kommuner.keys.firstOrNull { key -> kommuner[key] == kommunenavn }
        }.getOrNull()

    fun getPoststed(postnummer: String): String? = runCatching { kodeverkDataService.hentKodeverk(Postnummer)[postnummer] }.getOrNull()

    fun getLand(landkode: String): String? = runCatching { kodeverkDataService.hentKodeverk(Landkoder)[landkode] }.getOrNull()
}
