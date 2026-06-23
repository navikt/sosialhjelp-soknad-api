package no.nav.sosialhjelp.soknad.api.minesaker

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.v2.AntallSoknaderSendtValidator.Companion.MAX_ANTALL_SOKNADER
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
class MineSakerService(private val metadataService: SoknadMetadataService) {
    fun hentInnsendteSoknader(): List<SoknadMetadata> =
        metadataService.getAllMetadataForPerson(getUserIdFromToken())
            .filter { it.status == SoknadStatus.SENDT || it.status == SoknadStatus.MOTTATT_FSL }

    fun hentInnsendteSoknaderSisteDogn(): Pair<Int, LocalDateTime?> =
        metadataService.findMetadataForPersonSendtInnAfter(getUserIdFromToken(), nowWithMillis().minusDays(1))
            .also { if (it.size >= MAX_ANTALL_SOKNADER) logger.warn("Bruker har sendt inn ${it.size} søknader siste 24 timer") }
            .let { metadatas -> Pair(metadatas.size, metadatas.findInnsendingTillattFra()) }

    private fun List<SoknadMetadata>.findInnsendingTillattFra(): LocalDateTime? =
        if (size < MAX_ANTALL_SOKNADER) {
            return null
        } else {
            mapNotNull { it.tidspunkt.sendtInn }.sortedByDescending { it }[MAX_ANTALL_SOKNADER - 1]
                // ett døgn etter den MAX_ANTALL_SOKNADER nyeste søknaden
                .plusDays(1)
                // pluss ett minutt så bruker ikke må forholde seg til sekunder
                .plusMinutes(1)
                .truncatedTo(ChronoUnit.MINUTES)
        }

    companion object {
        private val logger by logger()
    }
}
