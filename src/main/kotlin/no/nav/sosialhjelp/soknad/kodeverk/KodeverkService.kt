package no.nav.sosialhjelp.soknad.kodeverk

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
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
        runCatching { kodeverkDataService.hentKodeverk(Kommuner)[kommunenummer] }
            .onFailure { logger.error("Feil ved henting av kommunenavn for kommunenummer $kommunenummer", it) }
            .getOrNull()

    fun gjettKommunenummer(kommunenavn: String): String? =
        runCatching {
            val kommuner = kodeverkDataService.hentKodeverk(Kommuner)
            kommuner.keys.firstOrNull { key -> kommuner[key] == kommunenavn }
        }
            .onFailure { logger.error("Feil ved gjetting av kommunenummer for kommunenavn $kommunenavn", it) }
            .getOrNull()

    fun getPoststed(postnummer: String): String? =
        runCatching { kodeverkDataService.hentKodeverk(Postnummer)[postnummer] }
            .onFailure { logger.error("Feil ved henting av poststed for postnummer $postnummer", it) }
            .getOrNull()

    fun getLand(landkode: String): String? =
        runCatching { kodeverkDataService.hentKodeverk(Landkoder)[landkode] }
            .onFailure { logger.error("Feil ved henting av land for landkode $landkode", it) }
            .getOrNull()

    companion object {
        private val logger by logger()
    }
}
