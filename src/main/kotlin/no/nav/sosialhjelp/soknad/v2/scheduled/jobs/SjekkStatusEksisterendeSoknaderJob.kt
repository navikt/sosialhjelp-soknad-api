package no.nav.sosialhjelp.soknad.v2.scheduled

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.service.AdresseService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.MOTTATT_FSL
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.SENDT
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadJobService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

/**
 * Dobbeltsjekker at gamle soknader ikke forblir med status sendt.
 */
@Component
class SjekkStatusEksisterendeSoknaderJob(
    private val soknadJobService: SoknadJobService,
    private val metadataService: SoknadMetadataService,
    private val adresseService: AdresseService,
    private val prometheusMetricsService: PrometheusMetricsService,
    leaderElection: LeaderElection,
) : AbstractJob(leaderElection, "Sjekke status eksisterende soknader", logger) {
    @Scheduled(cron = "0 0 7 * * *")
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
        return metadatas
            .filter { it.tidspunkt.sendtInn?.isBefore(definedTimestamp()) == true }
            .let {
                if (it.isNotEmpty()) handleGamleStatusSendt(it)
                prometheusMetricsService.setAntallGamleSoknaderStatusSendt(it.size)
                it.size
            }
    }

    private fun handleGamleStatusSendt(olderThan: List<SoknadMetadata>) {
        olderThan
            .map { metadata -> metadata.toSoknadInfo(navEnhet = adresseService.findMottaker(metadata.soknadId)) }
            .also {
                logger.error(
                    "Etter $NUMBER_OF_DAYS dager finnes det fortsatt ${it.size} soknader med status SENDT.\n " +
                        mapper.writeValueAsString(it),
                )
            }
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

private fun SoknadMetadata.toSoknadInfo(navEnhet: NavEnhet?) =
    SoknadInfo(
        id = this.soknadId,
        status = this.status,
        kommunenummer = this.mottakerKommunenummer ?: "Ukjent",
        soknadType = this.soknadType,
        navEnhet = navEnhet,
    )

data class SoknaderFeilStatusException(
    override val message: String,
) : IllegalStateException(message)

private data class SoknadInfo(
    val id: UUID,
    val status: SoknadStatus,
    val kommunenummer: String,
    val soknadType: SoknadType,
    val navEnhet: NavEnhet?,
)
