package no.nav.sosialhjelp.soknad.kodeverk

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class KodeverkStore(private val client: KodeverkClient) {
    @Cacheable("kodeverk")
    fun hentKodeverk(kodeverksnavn: String): Map<String, String?> {
        val kodeverk = client.hentKodeverk(kodeverksnavn)

        logger.info("Hentet kodeverk fra klient: ${mapper.writeValueAsString(kodeverk) }")

        return kodeverk.toMap()
            .also { "Returnerer map: ${mapper.writeValueAsString(it)}" }
    }

    companion object {
        private val logger by logger()

        private val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    }
}

private fun KodeverkDto.toMap(): Map<String, String?> =
    betydninger
        .map { it.key to it.value.firstOrNull()?.beskrivelser?.get(KodeverkClient.SPRÅK_NORSK_BOKMÅL)?.term }
        .toMap()
