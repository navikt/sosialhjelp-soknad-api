package no.nav.sosialhjelp.soknad.kodeverk

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class KodeverkStore(private val client: KodeverkClient) {
    @Cacheable("kodeverk")
    fun hentKodeverk(kodeverksnavn: String): Map<String, String?> {
        val kodeverk = client.hentKodeverk(kodeverksnavn)

        logger.info("Hentet kodeverk fra klient: ${jacksonObjectMapper().writeValueAsString(kodeverk) }")

        return kodeverk.toMap()
            .also { "Returnerer map: ${jacksonObjectMapper().writeValueAsString(it)}" }
    }

    companion object {
        private val logger by logger()
    }
}

private fun KodeverkDto.toMap(): Map<String, String?> =
    betydninger
        .map { it.key to it.value.firstOrNull()?.beskrivelser?.get(KodeverkClient.SPRÅK_NORSK_BOKMÅL)?.term }.toMap()
