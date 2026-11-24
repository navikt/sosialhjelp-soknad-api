package no.nav.sosialhjelp.soknad.v2.scheduled.jobs

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.innsending.digisosapi.FiksReadClient
import no.nav.sosialhjelp.soknad.metrics.SoknadMottattMetricsService
import no.nav.sosialhjelp.soknad.v2.metadata.GAMMEL_SOKNAD_ARBEIDSDAGER
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.scheduled.AbstractJob
import no.nav.sosialhjelp.soknad.v2.scheduled.LeaderElection
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SjekkStatusSendtJob(
    leaderElection: LeaderElection,
    private val metadataJobService: SoknadMetadataService,
    private val metricsService: SoknadMottattMetricsService,
    private val fiksReadClient: FiksReadClient,
): AbstractJob(leaderElection, JOB_NAME, logger) {

    @Scheduled(cron = "0 0 */4 * * MON-FRI")
    fun doCheckSoknaderStatusSendt() {
        doInJob {
            val soknaderTooOld = metadataJobService.findOldSoknaderStatusSendt()

            if (soknaderTooOld.isNotEmpty()) {
                logger.info("Fant ${soknaderTooOld.size} soknader med status SENDT som er eldre enn $GAMMEL_SOKNAD_ARBEIDSDAGER arbeidsdager. Sjekker om de har innsynsfil.")

                val soknaderHasInnsynsfil = soknaderTooOld.filter { it.hasInnsynsfil() }
                logger.info("Fant ${soknaderHasInnsynsfil.size} soknader med status SENDT som har innsynsfil. Oppdaterer status til MOTTATT_FSL.")
                soknaderHasInnsynsfil.forEach { it.markAsMottatt() }

                soknaderTooOld.minus(soknaderHasInnsynsfil.toSet())
                    .also { logger.info("Fant ${it.size} soknader med status SENDT som ikke har innsynsfil. Oppdaterer metrics.") }
            }

            metadataJobService.findOldSoknaderStatusSendt()
                .also { metricsService.setAntallGamleSoknaderStatusSendt(it.size) }
        }
    }

    // finnes digisosSak.digisosSoker.metadata - så finnes også innsynsfilen
    private fun SoknadMetadata.hasInnsynsfil(): Boolean {
        return runCatching { fiksReadClient.getDigisosSakMedSporingsId(getDigisosId()).digisosSoker?.metadata != null }
            .getOrElse { e ->
                logger.error("Feil ved henting av DigisosSak fra Fiks. digisosId=$digisosId", e)
                false
            }
    }

    private fun SoknadMetadata.markAsMottatt() {
        runCatching { metadataJobService.updateSoknadStatus(soknadId, SoknadStatus.MOTTATT_FSL) }
            .onFailure { e -> logger.error("Feil ved oppdatering av soknadId=$soknadId til status MOTTATT_FSL", e) }
    }

    private fun SoknadMetadata.getDigisosId() = digisosId ?: error("Soknad med id $soknadId har status SENDT, men digisosId er null. Kan ikke sjekke om innsynsfil finnes.")

    companion object {
        private val logger by logger()
        private val JOB_NAME = "Sjekk soknader ikke mottatt av FS"
    }
}

