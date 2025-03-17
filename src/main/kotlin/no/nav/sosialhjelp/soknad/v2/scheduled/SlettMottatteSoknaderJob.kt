package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.withTimeoutOrNull
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadJobService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@Component
class SlettMottatteSoknaderJob(
    private val leaderElection: LeaderElection,
    private val metadataService: SoknadMetadataService,
    private val soknadJobService: SoknadJobService,
    private val digisosApiService: DigisosApiService,
) {
    @Scheduled(cron = HVERT_MINUTT)
    suspend fun slettSoknaderSomErMottattAvFagsystem() {
        runCatching {
            if (leaderElection.isLeader()) {
                logger.info("Sletter søknader som er registret mottatt av fagsystem")

                withTimeoutOrNull(60.seconds) {
                    val metadatas = metadataService.getMetadatasStatusSendt()

                    metadatas
                        .mapNotNull { metadata -> metadata.digisosId }
                        .let { digisosIdsSendt -> digisosApiService.getDigisosIdsStatusMottatt(digisosIdsSendt) }
                        .let { digisosIdsMottatt -> metadatas.getSoknadIdsStatusMottatt(digisosIdsMottatt) }
                        .let { soknaderMottatt ->
                            val deleted = soknadJobService.deleteAllByIdCatchError(soknaderMottatt)
                            logger.info("Slettet $deleted mottatte soknader")
                            soknaderMottatt
                        }
                        .forEach { metadataService.updateSoknadStatus(it, SoknadStatus.MOTTATT_FSL) }
                }
                    ?: logger.error("Kunne ikke slette søknader som er registrert mottatt av fagsystem, tok for lang tid")
            }
        }.onFailure {
            logger.error("Feil ved sletting av søknader som er registrert mottatt av fagsystem", it)
        }
    }

    private fun List<SoknadMetadata>.getSoknadIdsStatusMottatt(digisosIds: List<UUID>): List<UUID> {
        return this
            .filter { digisosIds.contains(it.digisosId) }
            .map { it.soknadId }
    }

    companion object {
        private val logger by logger()
        private const val HVERT_MINUTT = "0 * * * * *"
    }
}
