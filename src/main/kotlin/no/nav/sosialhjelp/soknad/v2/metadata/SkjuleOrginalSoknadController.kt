package no.nav.sosialhjelp.soknad.v2.metadata

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionTokenXHigh
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID

// DSOS-649
// Pga. en bug hvor nye søknader fikk feil telefonnummer fra register, så skal vi ikke
// vise orginalsøknad for søknader som ble sendt inn i tidsrommet 28.04.26 14:32 til 29.04.26 12:30
// Disse søknadene ligger ikke lenger i databasen for at digisos-id ikke skal kunne knyttes til personnummer
// TODO Dette kan slettes når bruker ikke lenger kan se søknad i innsyn (hvor lenge?)

@RestController
@ProtectionTokenXHigh
@RequestMapping("/soknad/hide/{digisosId}")
class SkjuleOrginalSoknadController(
    private val metadataRepository: SoknadMetadataRepository,
) {
    @GetMapping
    fun skalSkjuleOriginalSoknad(
        @PathVariable digisosId: UUID,
    ): Boolean {
        logger.info("Sjekker om original soknad skal skjules for digisosId: $digisosId ")

        return runCatching {
            val metadata = metadataRepository.findMetadataByDigisosId(digisosId.toString()) ?: return true
            metadata.tidspunkt.opprettet.isInsideCriticalTimeslot()
        }
            .getOrElse {
                logger.error("SkalSkjuleOriginalSoknad feilet for digisosId: $digisosId", it)
                true
            }
    }

    private fun LocalDateTime.isInsideCriticalTimeslot() = isAfter(createdSafetyZoneStart) && isBefore(createdSafetyZoneEnd)

    companion object {
        private val logger by logger()
        val createdSafetyZoneStart: LocalDateTime = LocalDateTime.of(2026, 4, 28, 14, 15)
        val createdSafetyZoneEnd: LocalDateTime = LocalDateTime.of(2026, 4, 29, 12, 45)
    }
}
