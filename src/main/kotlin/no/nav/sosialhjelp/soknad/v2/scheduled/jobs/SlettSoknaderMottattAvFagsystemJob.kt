package no.nav.sosialhjelp.soknad.v2.scheduled.jobs

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.MOTTATT_FSL
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.SENDT
import no.nav.sosialhjelp.soknad.v2.scheduled.AbstractJob
import no.nav.sosialhjelp.soknad.v2.scheduled.LeaderElection
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadJobService
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID

@Profile("!local")
@Component
class SlettSoknaderMottattAvFagsystemJob(
    leaderElection: LeaderElection,
    private val metadataJobService: SoknadMetadataService,
    private val soknadJobService: SoknadJobService,
    private val digisosApiService: DigisosApiService,
) : AbstractJob(leaderElection, "Slette mottatte soknader", logger) {
    @Scheduled(cron = "0 */10 * * * *")
    fun slettSoknaderSomErMottattAvFagsystem() = doInJob { findAndDeleteMottatteSoknader() }

    private fun findAndDeleteMottatteSoknader() {
        val metadatas = metadataJobService.findMetadataForStatus(SENDT)

        metadatas
            .mapNotNull { metadata -> metadata.getDigisosId() }
            .let { digisosIdsSendt -> digisosApiService.getDigisosIdsStatusMottatt(digisosIdsSendt) }
            .let { digisosIdsMottatt -> metadatas.findSoknadIdsStatusMottatt(digisosIdsMottatt) }
            .also { soknadIdsMottatt -> handleMottatteIds(soknadIdsMottatt) }
    }

    // Kastes det exception her...
    // ... vil det stoppe opp oppdateringen for mange andre objekter også
    private fun SoknadMetadata.getDigisosId(): UUID? =
        digisosId.also { if (digisosId == null) logger.error("DigisosId er null for $soknadId") }

    private fun List<SoknadMetadata>.findSoknadIdsStatusMottatt(digisosIds: List<UUID>): List<UUID> {
        return this
            .filter { digisosIds.contains(it.digisosId) }
            .map { it.soknadId }
    }

    private fun handleMottatteIds(mottatteIds: List<UUID>) {
        if (mottatteIds.isNotEmpty()) {
            soknadJobService.deleteSoknaderByIds(mottatteIds)
            logger.info("Slettet ${mottatteIds.size} med status MOTTATT hos FIKS")
            mottatteIds.forEach { metadataJobService.updateSoknadStatus(it, MOTTATT_FSL) }
        }
    }

    companion object {
        private val logger by logger()
    }
}
