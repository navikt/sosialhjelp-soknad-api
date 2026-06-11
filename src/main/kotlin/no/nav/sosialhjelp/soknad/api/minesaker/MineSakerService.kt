package no.nav.sosialhjelp.soknad.api.minesaker

import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
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

    fun hentInnsendteSoknaderSisteDogn(): Pair<Int, LocalDateTime?> {
        val cutOff = nowWithMillis().minusDays(1)

        return metadataService.getAllMetadataForPerson(getUserIdFromToken())
            .filter { it.status == SoknadStatus.SENDT || it.status == SoknadStatus.MOTTATT_FSL }
            .filter { it.tidspunkt.sendtInn?.isAfter(cutOff) ?: error("Fant ikke tidspunkt for innsending") }
            .let { metadatas ->
                Pair(
                    first = metadatas.size,
                    second =
                        if (metadatas.size >= 10) {
                            metadatas.findInnsendingTillattFra()
                        } else {
                            null
                        },
                )
            }
    }
}

private fun List<SoknadMetadata>.findInnsendingTillattFra(): LocalDateTime =
    this.mapNotNull { it.tidspunkt.sendtInn }.sortedByDescending { it }[9].plusDays(1)
