package no.nav.sosialhjelp.soknad.v2.scheduled

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.OPPRETTET
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadJobService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

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
        soknadJobService.findAllSoknadIds()
            .let { ids -> metadataService.findAllMetadatasForIds(ids) }
            .filter { metadatas -> metadatas.status != OPPRETTET }
            .also { notOpprettet -> if (notOpprettet.isNotEmpty()) handleGamleSoknader(notOpprettet) }
    }

    private fun handleGamleSoknader(metadatas: List<SoknadMetadata>) {
        val soknadIdToStatus = metadatas.map { Pair(it.soknadId, it.status) }

        logger.error(
            "Det eksisterer fortsatt ${metadatas.size} soknader med feil status. \n" +
                soknadIdToStatus.joinToString(separator = ";"),
        )
        throw EksisterendeSoknaderStatusException(
            message = "Det finnes ${metadatas.size} eksisterende soknader med feil status: ",
        )
    }

    companion object {
        private val logger by logger()
        private const val HVER_TIME: String = "0 0 * * * *"
    }
}

data class EksisterendeSoknaderStatusException(
    override val message: String,
) : IllegalStateException(message)
