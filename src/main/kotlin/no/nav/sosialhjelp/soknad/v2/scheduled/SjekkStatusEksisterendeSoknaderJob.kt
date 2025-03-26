package no.nav.sosialhjelp.soknad.v2.scheduled

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.OPPRETTET
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
    private val leaderElection: LeaderElection,
) {
    @Scheduled(cron = HVER_TIME)
    fun sjekkStatus() {
        if (leaderElection.isLeader()) {
            soknadJobService.findAllSoknadIds()
                .let { ids -> metadataService.findAllMetadatasForIds(ids) }
                .filter { metadatas -> metadatas.status != OPPRETTET }
                .also { notOpprettet -> if (notOpprettet.isNotEmpty()) handleGamleSoknader(notOpprettet) }
        }
    }

    private fun handleGamleSoknader(metadatas: List<SoknadMetadata>) {
        val numberOfSoknaderWrongStatus =
            handleStatusSendt(metadatas.filter { it.status == SENDT }) +
                handleOther(metadatas.filter { it.status != OPPRETTET })

        if (numberOfSoknaderWrongStatus != 0) {
            throw SoknaderFeilStatusException("Det finnes $numberOfSoknaderWrongStatus med feil status")
        }
    }

    // tar vare på data for sendte soknader en stund da FSL tilsynelatende ikke kvitterer ut med en gang
    private fun handleStatusSendt(metadatas: List<SoknadMetadata>): Int {
        val olderThan = metadatas.filter { it.tidspunkt.sendtInn?.isBefore(definedTimestamp()) ?: false }

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

    // skal ikke finnes eksisterende soknader for andre statuser
    private fun handleOther(metadatas: List<SoknadMetadata>): Int {
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
        private const val HVER_TIME: String = "0 0 * * * *"
        private const val NUMBER_OF_DAYS = 7L
        private val mapper = jacksonObjectMapper()
    }
}

data class SoknaderFeilStatusException(
    override val message: String,
) : IllegalStateException(message)
