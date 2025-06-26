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
    private val metadataService: SoknadMetadataService,
    private val soknadJobService: SoknadJobService,
    private val digisosApiService: DigisosApiService,
) : AbstractJob(leaderElection, "Slette mottatte soknader") {
    @Scheduled(cron = "0 */10 * * * *")
    suspend fun slettSoknaderSomErMottattAvFagsystem() =
        doInJob {
            val metadatas = getExistingMetadatasStatusSendt()
            metadatas
                .mapNotNull { metadata -> metadata.getDigisosId() }
                .let { digisosIdsSendt -> digisosApiService.getDigisosIdsStatusMottatt(digisosIdsSendt) }
                .let { digisosIdsMottatt -> metadatas.filterSoknadIdsStatusMottat(digisosIdsMottatt) }
                .also { soknadIdsMottatt -> handleMottatteIds(soknadIdsMottatt) }
        }

    private fun getExistingMetadatasStatusSendt(): List<SoknadMetadata> =
        soknadJobService.findSoknadIdsWithStatus(SENDT).let { metadataService.getMetadatasForIds(it) }

    // ... vil det stoppe opp oppdateringen for mange andre objekter også
    private fun SoknadMetadata.getDigisosId(): UUID? =
        digisosId
            .also { if (digisosId == null) logger.error("DigisosId er null for $soknadId") }

    private fun List<SoknadMetadata>.filterSoknadIdsStatusMottat(digisosIds: List<UUID>): List<UUID> {
        return this
            .filter { digisosIds.contains(it.digisosId) }
            .map { it.soknadId }
    }

    private fun handleMottatteIds(mottatteIds: List<UUID>) {
        if (mottatteIds.isNotEmpty()) {
            logger.info("Sletter ${mottatteIds.size} med status MOTTATT hos FIKS")
            soknadJobService.deleteSoknaderByIds(mottatteIds)
            mottatteIds.forEach { metadataService.updateSoknadStatus(it, MOTTATT_FSL) }
        }
    }

    companion object {
        private val logger by logger()
    }
}
