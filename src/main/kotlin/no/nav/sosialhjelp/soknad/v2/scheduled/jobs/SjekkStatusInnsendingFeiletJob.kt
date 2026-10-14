package no.nav.sosialhjelp.soknad.v2.scheduled.jobs

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiJobService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.InnsendingStatusDto
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataJobService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.scheduled.AbstractJob
import no.nav.sosialhjelp.soknad.v2.scheduled.LeaderElection
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class SjekkStatusInnsendingFeiletJob(
    leaderElection: LeaderElection,
    private val metadataJobService: SoknadMetadataJobService,
    private val digisosApiJobService: DigisosApiJobService,
) : AbstractJob(leaderElection, JOB_NAME, logger) {
    @Scheduled(cron = "0 */10 * * * *")
    fun sjekkStatusInnsendingFeilet() = doInJob { findAndCheckInnsendingFeilet() }

    private fun findAndCheckInnsendingFeilet() {
        metadataJobService.findMetadataForStatus(SoknadStatus.INNSENDING_FEILET)
            .takeIf { it.isNotEmpty() }
            ?.also {
                logger.info("Fant ${it.size} soknader hvor innsending feilet. Sjekker om status kan fikses")
                checkFiksStatusInnsendingFeilet(it)
            }
    }

    private fun checkFiksStatusInnsendingFeilet(metadata: List<SoknadMetadata>) {
        val mottatteSoknader = digisosApiJobService.checkSoknaderMottatt(metadata.map { it.soknadId })
        if (mottatteSoknader.isEmpty()) return

        val relevanteMetadata =
            mottatteSoknader
                .map { it.navEksternRefId }
                .let { mottatteIds -> metadata.filter { it.soknadId in mottatteIds } }

        updateMetadata(relevanteMetadata, mottatteSoknader)
    }

    private fun updateMetadata(
        relevanteMetadata: List<SoknadMetadata>,
        mottattFiks: List<InnsendingStatusDto>,
    ) {
        logger.info("Fant ${relevanteMetadata.size} soknader som er mottatt hos FIKS. Oppdaterer metadata")

        relevanteMetadata
            .map { metadata ->
                val mottattSoknad =
                    mottattFiks.find { mottatt -> mottatt.navEksternRefId == metadata.soknadId }
                        ?: error("Fant ikke mottatt soknad for ${metadata.soknadId}")

                val tidspunktOpprettetFiks = mottattSoknad.opprettet.toLocalDateTime()

                metadata.copy(
                    status = SoknadStatus.SENDT,
                    digisosId = mottattSoknad.digisosId,
                    mottakerKommunenummer = mottattSoknad.kommunenummer,
                    tidspunkt =
                        metadata.tidspunkt.copy(
                            sendtInn = tidspunktOpprettetFiks,
                            sistEndret = tidspunktOpprettetFiks,
                        ),
                )
            }
            .let { metadatas -> metadataJobService.saveMetadatas(metadatas) }
    }

    companion object {
        private val logger by logger()
        private const val JOB_NAME = "Sjekk Fiks-status for INNSENDING_FEILET"
    }
}

private fun Long.toLocalDateTime() = TimestampUtil.convertInstantToLocalDateTime(Instant.ofEpochMilli(this))
