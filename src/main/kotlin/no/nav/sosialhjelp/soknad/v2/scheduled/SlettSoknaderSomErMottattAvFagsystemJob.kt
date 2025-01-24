package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.withTimeoutOrNull
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.seconds

@Component
class SlettSoknaderSomErMottattAvFagsystemJob(
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val soknadRepository: SoknadRepository,
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
                                soknadMetadataRepository.hentSoknadIderMedStatus(SoknadStatus.SENDT),
                            )

                        soknadIderSomKanSlettes.forEach {
                            // TODO trenger vi egentlig denne? Heller ha en logg statement til slutt som sier hvor mange som ble slettet? Evt greit å ha denne i starten?
                            logger.info("Slettet soknad med id $it")
                            soknadRepository.deleteById(it)
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
