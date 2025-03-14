package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.withTimeoutOrNull
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadIdToDigisosId
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@Component
class SlettMottatteSoknaderJob(
    private val leaderElection: LeaderElection,
    private val metadataService: SoknadMetadataService,
    private val soknadService: SoknadService,
    private val digisosApiService: DigisosApiService,
) {
    @Scheduled(cron = HVERT_MINUTT)
    suspend fun slettSoknaderSomErMottattAvFagsystem() {
        runCatching {
            if (leaderElection.isLeader()) {
                logger.info("Sletter søknader som er registret mottatt av fagsystem")

                withTimeoutOrNull(60.seconds) {
                    val soknadIdsToDigisosIds = metadataService.getMetadatasStatusSendt()

                    soknadIdsToDigisosIds
                        .map { idMap -> idMap.digisosId }
                        .let { digisosIdsSendt -> digisosApiService.getSoknaderStatusMottatt(digisosIdsSendt) }
                        .let { digisosIdsMottatt -> soknadIdsToDigisosIds.filterIdsStatusMottatt(digisosIdsMottatt) }
                        .forEach { soknadId -> deleteAndUpdateMetadata(soknadId) }
                }
                    ?: logger.error("Kunne ikke slette søknader som er registrert mottatt av fagsystem, tok for lang tid")
            }
        }.onFailure {
            logger.error("Feil ved sletting av søknader som er registrert mottatt av fagsystem", it)
        }
    }

    private fun List<SoknadIdToDigisosId>.filterIdsStatusMottatt(digisosIds: List<UUID>): List<UUID> {
        return this
            .filter { digisosIds.contains(it.digisosId) }
            .map { it.soknadId }
    }

    private fun deleteAndUpdateMetadata(soknadId: UUID) {
        soknadService.deleteSoknad(soknadId)
        logger.info("Slettet soknad med Id $soknadId")
        metadataService.updateSoknadStatus(soknadId, SoknadStatus.MOTTATT_FSL)
    }

    private fun getRemainingStatusSendt(soknadIdsToDigisosIds: List<SoknadIdToDigisosId>): List<SoknadMetadata> {
        return soknadIdsToDigisosIds
            .map { it.soknadId }
            .let { metadataService.getAllMetadataForSoknadIds(it) }
            .filter { it.tidspunkt.isOlderThan(DAYS) && it.status == SoknadStatus.SENDT }
    }

    private fun writeErrorOnRemainingStatusSendt(metadataList: List<SoknadMetadata>) {
        logger.error(
            "Det finnes over en dag gamle soknader som ikke er sendt: " +
                "${metadataList.map { it.soknadId to it.digisosId }}\\n",
        )
    }

    companion object {
        private val logger by logger()
        private const val HVERT_MINUTT = "0 * * * * *"
        private const val DAYS = 1L
    }
}

fun Tidspunkt.isOlderThan(days: Long): Boolean {
    return this.sendtInn?.isBefore(LocalDateTime.now().minusDays(days))
        ?: error("Soknad mangler tidspunkt for innsendt")
}
