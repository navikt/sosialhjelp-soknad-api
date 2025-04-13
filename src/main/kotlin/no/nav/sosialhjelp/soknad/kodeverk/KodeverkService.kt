package no.nav.sosialhjelp.soknad.kodeverk

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.kodeverk.Kodeverksnavn.KOMMUNER
import no.nav.sosialhjelp.soknad.kodeverk.Kodeverksnavn.LANDKODER
import no.nav.sosialhjelp.soknad.kodeverk.Kodeverksnavn.POSTNUMMER
import org.springframework.stereotype.Service

@Service
class KodeverkService(private val kodeverkStore: KodeverkStore) {
    fun getKommunenavn(kommunenummer: String): String? = hentKodeverk(KOMMUNER)[kommunenummer]

    fun gjettKommunenummer(kommunenavn: String): String? =
        hentKodeverk(KOMMUNER).entries.find { it.value == kommunenavn }?.key

    fun getPoststed(postnummer: String): String? = hentKodeverk(POSTNUMMER)[postnummer]

    fun getLand(landkode: String): String? = hentKodeverk(LANDKODER)[landkode]

    private fun hentKodeverk(navn: Kodeverksnavn): Map<String, String?> {
        return doHentKodeverk { kodeverkStore.hentKodeverk(navn.value) }
            ?: doHentKodeverk { kodeverkStore.hentKodeverkNoCache(navn.value) }
            ?: emptyMap()
    }

    private fun doHentKodeverk(getKodeverkFunc: () -> Map<String, String?>): Map<String, String?>? =
        runCatching { getKodeverkFunc.invoke().takeIf { it.isNotEmpty() } }
            .onFailure { logger.error("Feil ved henting av Kodeverk", it) }
            .getOrNull()

    companion object {
        private val logger by logger()
    }
}

enum class Kodeverksnavn(val value: String) {
    POSTNUMMER("Postnummer"),
    KOMMUNER("Kommuner"),
    LANDKODER("Landkoder"),
}
