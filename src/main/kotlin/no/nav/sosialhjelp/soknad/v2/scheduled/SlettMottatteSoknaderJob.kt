package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.withTimeoutOrNull
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.seconds

@Component
class SlettMottatteSoknaderJob(
    private val soknadService: SoknadService,
    private val soknadMetadataService: SoknadMetadataService,
    private val digisosApiService: DigisosApiService,
    private val leaderElection: LeaderElection,
) {
    @Scheduled(cron = HVERT_MINUTT)
    suspend fun slettSoknaderSomErMottattAvFagsystem() {
        runCatching {
            if (leaderElection.isLeader()) {
                logger.info("Sletter søknader som er registret mottatt av fagsystem")

                val result =
                    withTimeoutOrNull(60.seconds) {
                        val soknadIderSomKanSlettes =
                            digisosApiService.getSoknaderMedStatusMotattFagsystem(
                                soknadMetadataService.getIDForSoknaderMedStatusSendt(),
                            )

                        soknadIderSomKanSlettes.forEach {
                            // TODO trenger vi egentlig denne log statementen? Heller ha en logg statement til slutt som sier hvor mange som ble slettet? Evt greit å ha denne i starten?
                            logger.info("Slettet soknad med id $it")
                            soknadService.deleteSoknad(it)
                            soknadMetadataService.updateSoknadStatus(it, SoknadStatus.MOTTATT_FSL)
                        }
                    }
                if (result == null) {
                    logger.error("Kunne ikke slette søknader som er registrert mottatt av fagsystem, tok for lang tid")
                }
            }
        }.onFailure {
            logger.error("Feil ved sletting av søknader som er registrert mottatt av fagsystem", it)
        }
    }

    companion object {
        private val logger by logger()
        private const val HVERT_MINUTT = "0 * * * * *"
    }
}
