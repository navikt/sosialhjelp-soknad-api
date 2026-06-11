package no.nav.sosialhjelp.soknad.api.minesaker

import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class MineSakerService(private val metadataService: SoknadMetadataService) {
    fun hentInnsendteSoknader(): List<SoknadMetadata> =
        metadataService.getAllMetadataForPerson(getUserIdFromToken())
            .filter { it.status == SoknadStatus.SENDT || it.status == SoknadStatus.MOTTATT_FSL }

    fun hentInnsendteSoknaderSisteDogn() {
        val cutOff = LocalDateTime.now().minusDays(1)

        metadataService.getAllMetadataForPerson(getUserIdFromToken())
            .filter { it.status == SoknadStatus.SENDT || it.status == SoknadStatus.MOTTATT_FSL }
            .filter { it.tidspunkt.sendtInn?.isAfter(cutOff) ?: false }
            .let { metadatas ->
                Pair(
                    first = metadatas.size,
                    second =
                        if (metadatas.size == 10) {
                            metadatas.minOfOrNull { it.tidspunkt.sendtInn ?: error("Sendt inn var null") }
                        } else {
                            null
                        },
                )
            }
    }
}
