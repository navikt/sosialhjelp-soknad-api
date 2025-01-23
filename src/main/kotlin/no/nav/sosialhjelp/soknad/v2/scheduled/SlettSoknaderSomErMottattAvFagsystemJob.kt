package no.nav.sosialhjelp.soknad.v2.scheduled

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SlettSoknaderSomErMottattAvFagsystemJob(
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val soknadRepository: SoknadRepository,
    private val digisosApiService: DigisosApiService,
) {
    @Scheduled(cron = HVERT_MINUTT)
    fun slettSoknaderSomErMottattAvFagsystem() {
        logger.info("SlettSoknaderSomErMottattAvFagsystem")

        val soknadIderSomKanSlettes = digisosApiService.getSoknaderMedStatusMotattFagsystem(soknadMetadataRepository.hentSoknadIderMedStatus(SoknadStatus.SENDT))

        soknadIderSomKanSlettes.forEach {
            logger.info("Slettet soknad med id $it")
            soknadRepository.deleteById(it)
        }
    }

    companion object {
        private val logger by logger()
        private const val HVERT_MINUTT = "0 * * * * *"
    }
}
