package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.withTimeoutOrNull
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentlagerService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@Component
class SlettGamleSoknaderJob(
    private val leaderElection: LeaderElection,
    // TODO Bruke service fremfor repository direkte, pga debug/sporing?
    private val soknadRepository: SoknadRepository,
    private val metadataRepository: SoknadMetadataRepository,
    private val dokumentlagerService: DokumentlagerService,
) {
    @Scheduled(cron = KLOKKEN_TRE_OM_NATTEN)
    suspend fun slettGamleSoknader() {
        runCatching {
            if (leaderElection.isLeader()) {
                withTimeoutOrNull(60.seconds) {
                    metadataRepository.findOlderThan(LocalDateTime.now().minusDays(14))
                        .also {
                            soknadRepository.deleteAllById(it)
                            logger.info("Slettet $it gamle søknader")
                            slettFilerForSoknader(it)
                        }
                }
                    ?: logger.error("Kunne ikke slette gamle søknader, tok for lang tid")
            }
        }
            .onFailure { logger.error("Feil ved sletting av gamle søknader", it) }
    }

    private fun slettFilerForSoknader(oldUuids: List<UUID>) {
        oldUuids.forEach { uuid ->
            runCatching { dokumentlagerService.deleteAllDokumenterForSoknad(uuid) }
                .onFailure { ex ->
                    logger.warn(
                        "Slette gamle soknaer: Feil eller fantes ingen filer hos FIKS for: $uuid",
                        ex,
                    )
                }
        }
    }

    companion object {
        private const val KLOKKEN_TRE_OM_NATTEN = "0 0 3 * * *"
        private val logger by logger()
    }
}
