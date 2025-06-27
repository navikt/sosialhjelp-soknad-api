package no.nav.sosialhjelp.soknad.v2.scheduled

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.MOTTATT_FSL
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.SENDT
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
    leaderElection: LeaderElection,
) : AbstractJob(leaderElection, "Sjekke status eksisterende soknader", logger) {
    @Scheduled(cron = "0 0 * * * *")
    suspend fun checkIfExistingSoknaderHasWrongStatus() = doInJob { findSoknadWithWrongStatus() }

    private fun findSoknadWithWrongStatus() {
        soknadJobService.findAllSoknadIds()
            .let { ids -> metadataService.findAllMetadatasForIds(ids) }
            .filter { metadata -> filterRelevantStatus(metadata) }
            .also { relevantSoknader -> if (relevantSoknader.isNotEmpty()) handleGamleSoknader(relevantSoknader) }
    }

    private fun filterRelevantStatus(metadatas: SoknadMetadata) =
        metadatas.status == SENDT || metadatas.status == MOTTATT_FSL

    private fun handleGamleSoknader(metadatas: List<SoknadMetadata>) {
        val nrOfSendt = checkStatusSendt(metadatas.filter { it.status == SENDT })
        val nrOfMottatt = checkStatusMottatt(metadatas.filter { it.status == MOTTATT_FSL })

        if (nrOfSendt + nrOfMottatt > 0) {
            throw SoknaderFeilStatusException("Det finnes eksisterende soknader med feil status")
        }
    }

    private fun checkStatusSendt(metadatas: List<SoknadMetadata>): Int {
        // gir kommunen noen dager på å kvittere ut
        val olderThan = metadatas.filter { it.tidspunkt.sendtInn?.isBefore(definedTimestamp()) == true }

        if (olderThan.isNotEmpty()) {
            olderThan
                .map { Pair(it.soknadId, it.status) }
                .also {
                    logger.error(
                        "Etter $NUMBER_OF_DAYS dager finnes det fortsatt ${it.size} soknader med status SENDT.\n " +
                            mapper.writeValueAsString(it),
                    )
                }
        }
        return olderThan.size
    }

    // skal ikke finnes eksisterende soknader med status mottatt
    private fun checkStatusMottatt(metadatas: List<SoknadMetadata>): Int {
        if (metadatas.isNotEmpty()) {
            metadatas
                .map { Pair(it.soknadId, it.status) }
                .also { logger.error("Eksisterende soknader med feil status: \n" + mapper.writeValueAsString(it)) }
        }
        return metadatas.size
    }

    private fun definedTimestamp(): LocalDateTime = LocalDateTime.now().minusDays(NUMBER_OF_DAYS)

    companion object {
        private val logger by logger()
        private const val NUMBER_OF_DAYS = 7L
        private val mapper = jacksonObjectMapper()
    }
}

data class SoknaderFeilStatusException(
    override val message: String,
) : IllegalStateException(message)
