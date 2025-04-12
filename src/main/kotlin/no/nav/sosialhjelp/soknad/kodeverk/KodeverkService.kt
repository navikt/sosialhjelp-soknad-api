package no.nav.sosialhjelp.soknad.kodeverk

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.kodeverk.Kodeverksnavn.KOMMUNER
import no.nav.sosialhjelp.soknad.kodeverk.Kodeverksnavn.LANDKODER
import no.nav.sosialhjelp.soknad.kodeverk.Kodeverksnavn.POSTNUMMER
import org.springframework.stereotype.Service

@Service
class KodeverkService(private val kodeverkStore: KodeverkStore) {
    fun getKommunenavn(kommunenummer: String): String? = doHentKodeverk(KOMMUNER)[kommunenummer]

    fun gjettKommunenummer(kommunenavn: String): String? =
        doHentKodeverk(KOMMUNER).entries.find { it.value == kommunenavn }?.key

    fun getPoststed(postnummer: String): String? = doHentKodeverk(POSTNUMMER)[postnummer]

    fun getLand(landkode: String): String? = doHentKodeverk(LANDKODER)[landkode]

    private fun doHentKodeverk(navn: Kodeverksnavn): Map<String, String?> =
        runCatching { kodeverkStore.hentKodeverk(navn.value) }
            .onFailure { logger.error("Kunne ikke hente Kodeverk", it) }
            .getOrElse { emptyMap() }

    companion object {
        private val logger by logger()
    }
}

enum class Kodeverksnavn(val value: String) {
    POSTNUMMER("Postnummer"),
    KOMMUNER("Kommuner"),
    LANDKODER("Landkoder"),
}
