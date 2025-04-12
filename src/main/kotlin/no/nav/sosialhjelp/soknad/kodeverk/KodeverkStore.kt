package no.nav.sosialhjelp.soknad.kodeverk

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class KodeverkStore(private val client: KodeverkClient) {
    @Cacheable("kodeverk")
    fun hentKodeverk(kodeverksnavn: String): Map<String, String?> {
        return client.hentKodeverk(kodeverksnavn).toMap()
    }
}

private fun KodeverkDto.toMap(): Map<String, String?> =
    betydninger
        .map { it.key to it.value.firstOrNull()?.beskrivelser?.get(KodeverkClient.SPRÅK_NORSK_BOKMÅL)?.term }
        .toMap()
