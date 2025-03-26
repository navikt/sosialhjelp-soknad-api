package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.withTimeoutOrNull
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.MOTTATT_FSL
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.SENDT
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadJobService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@Component
class SlettSoknaderMottattAvFagsystemJob(
    private val leaderElection: LeaderElection,
    private val metadataService: SoknadMetadataService,
    private val soknadJobService: SoknadJobService,
    private val digisosApiService: DigisosApiService,
) {
    @Scheduled(cron = HVERT_MINUTT)
    suspend fun slettSoknaderSomErMottattAvFagsystem() {
        runCatching {
            if (leaderElection.isLeader()) {
                withTimeoutOrNull(60.seconds) {
                    val metadatas = getExistingMetadatasStatusSendt()
                    metadatas
                        .mapNotNull { metadata -> metadata.getDigisosId() }
                        .let { digisosIdsSendt -> digisosApiService.getDigisosIdsStatusMottatt(digisosIdsSendt) }
                        .let { digisosIdsMottatt -> metadatas.filterSoknadIdsStatusMottat(digisosIdsMottatt) }
                        .also { soknadIdsMottatt -> handleMottatteIds(soknadIdsMottatt) }
                }
                    ?: logger.error("Kunne ikke slette søknader som er registrert mottatt av fagsystem, tok for lang tid")
            }
        }.onFailure {
            logger.error("Feil ved sletting av søknader som er registrert mottatt av fagsystem", it)
        }
    }

    private fun getExistingMetadatasStatusSendt(): List<SoknadMetadata> =
        soknadJobService.findSoknadIdsWithStatus(SENDT).let { metadataService.getMetadatasForIds(it) }

    // TODO Metadata med denne statusen uten DigisosId skal egentlig ikke kunne skje, men kaster vi en feil her...
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
        private const val HVERT_MINUTT = "0 * * * * *"
    }
}
