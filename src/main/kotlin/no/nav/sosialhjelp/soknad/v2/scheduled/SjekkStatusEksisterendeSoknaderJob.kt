package no.nav.sosialhjelp.soknad.v2.scheduled

import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadJobService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * Dobbeltsjekker at gamle soknader ikke forblir med status sendt.
 */
@Component
class SjekkStatusEksisterendeSoknaderJob(
    private val soknadJobService: SoknadJobService,
    private val metadataService: SoknadMetadataService,
) {
    @Scheduled(cron = HVER_TIME)
    fun sjekkStatus() {
        metadataService.findForIdsOlderThan(
            soknadIds = soknadJobService.getAllSoknader().map { it.id },
            timestamp = getTimestamp(),
        )
            .filter { it.status == SoknadStatus.SENDT || it.status == SoknadStatus.MOTTATT_FSL }
            .also {
                if (it.isNotEmpty()) handleGamleSoknader(it)
            }
    }

    private fun handleGamleSoknader(metadatas: List<SoknadMetadata>) {
        throw EksisterendeSoknaderStatusException(
            message = "Det finnes eksisterende soknader med feil status: ",
            metadatas =
                metadatas
                    .map { "{ SoknadId: ${it.soknadId},  DigisosId: ${it.digisosId}, Status: ${it.status}" },
        )
    }

    companion object {
        private fun getTimestamp() = LocalDateTime.now().minusDays(NUMBER_OF_DAYS)

        private const val HVER_TIME: String = "0 0 * * * *"
        private const val NUMBER_OF_DAYS = 1L
    }
}

data class EksisterendeSoknaderStatusException(
    override val message: String,
    val metadatas: List<String>,
) : IllegalStateException(message + metadatas.joinToString(separator = "\n"))
