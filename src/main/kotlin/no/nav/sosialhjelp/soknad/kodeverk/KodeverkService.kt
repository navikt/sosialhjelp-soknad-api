package no.nav.sosialhjelp.soknad.kodeverk

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.kodeverk.dto.KodeverkDto
import org.springframework.stereotype.Service

typealias Kodeverk = Map<String, String?>

@Service
class KodeverkService(private val kodeverkClient: KodeverkClient) {
    fun getKommunenavn(kommunenummer: String): String? = doHentKodeverk(Kommuner)[kommunenummer]

    fun gjettKommunenummer(kommunenavn: String): String? =
        doHentKodeverk(Kommuner).entries.find { it.value == kommunenavn }?.key

    fun getPoststed(postnummer: String): String? = doHentKodeverk(Postnummer)[postnummer]

    fun getLand(landkode: String): String? = doHentKodeverk(Landkoder)[landkode]

    private fun doHentKodeverk(kodeverksnavn: String): Kodeverk {
        return runCatching {
            kodeverkClient.hentKodeverk(kodeverksnavn).toMap()
        }
            .onFailure { logger.error("Kunne ikke hente Kodeverk", it) }
            .getOrNull() ?: emptyMap()
    }

    companion object {
        private val logger by logger()

        private val Postnummer = "Postnummer"
        private val Kommuner = "Kommuner"
        private val Landkoder = "Landkoder"
    }
}

private fun KodeverkDto.toMap(): Kodeverk =
    this.betydninger
        .map { it.key to it.value.firstOrNull()?.beskrivelser?.get(KodeverkClient.SPRÅK_NORSK_BOKMÅL)?.term }
        .toMap()
