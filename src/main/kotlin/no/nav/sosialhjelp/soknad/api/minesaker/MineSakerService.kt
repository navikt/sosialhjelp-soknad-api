package no.nav.sosialhjelp.soknad.api.minesaker

import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
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
            .let { metadatas -> Pair(metadatas.size, metadatas.findInnsendingTillattFra()) }
}

private fun List<SoknadMetadata>.findInnsendingTillattFra(): LocalDateTime? =
    if (size < 10) {
        return null
    } else {
        mapNotNull { it.tidspunkt.sendtInn }.sortedByDescending { it }[9]
            // ett døgn etter den 10 nyeste søknaden
            .plusDays(1)
            // pluss ett minutt så bruker ikke må forholde seg til sekunder
            .plusMinutes(1)
            .truncatedTo(ChronoUnit.MINUTES)
    }
