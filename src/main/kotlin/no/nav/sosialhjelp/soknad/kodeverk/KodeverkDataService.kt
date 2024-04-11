package no.nav.sosialhjelp.soknad.kodeverk

import no.nav.sosialhjelp.soknad.kodeverk.dto.KodeverkDto
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

typealias Kodeverk = Map<String, String?>

@Service
class KodeverkDataService(
    private val kodeverkClient: KodeverkClient,
) {
    @Cacheable("kodeverk")
    fun hentKodeverk(kodeverkNavn: String): Kodeverk = kodeverkClient.hentKodeverk(kodeverkNavn).toMap()

    private fun KodeverkDto.toMap(): Kodeverk =
        this.betydninger.map { it.key to it.value.firstOrNull()?.beskrivelser?.get(KodeverkClient.SPRÅK_NORSK_BOKMÅL)?.term }.toMap()

    companion object {
        val Postnummer = "Postnummer"
        val Kommuner = "Kommuner"
        val Landkoder = "Landkoder"
    }
}
