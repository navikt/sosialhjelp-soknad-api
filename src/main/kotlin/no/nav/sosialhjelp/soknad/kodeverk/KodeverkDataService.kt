package no.nav.sosialhjelp.soknad.kodeverk

import no.nav.sosialhjelp.soknad.kodeverk.dto.KodeverkDto
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

typealias Kodeverk = Map<String, String?>

@Cacheable("kodeverk")
@Service
class KodeverkDataService(
    private val kodeverkClient: KodeverkClient,
) {
    fun hentKodeverk(kodeverkNavn: String): Kodeverk = kodeverkClient.hentKodeverk(kodeverkNavn).toMap()

    private fun KodeverkDto.toMap(): Kodeverk =
        this.betydninger.map { it.key to it.value.first().beskrivelser[KodeverkClient.SPRÅK_NORSK_BOKMÅL]?.term }.toMap()

    companion object {
        val Postnummer = "postnummer"
        val Kommuner = "kommuner"
        val Landkoder = "landkoder"
    }
}
