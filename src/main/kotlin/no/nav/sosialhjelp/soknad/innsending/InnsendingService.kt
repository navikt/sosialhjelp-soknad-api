package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
class InnsendingService(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
    private val soknadMetadataRepository: SoknadMetadataRepository
) {
    fun oppdaterSoknadUnderArbeid(soknadUnderArbeid: SoknadUnderArbeid?) {
        check(soknadUnderArbeid != null) { "Kan ikke sende søknad som ikke finnes eller som mangler søknadsid" }

        soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(
            soknadUnderArbeid,
            LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        )
        soknadUnderArbeid.status = SoknadUnderArbeidStatus.LAAST
        soknadUnderArbeidRepository.oppdaterInnsendingStatus(soknadUnderArbeid, soknadUnderArbeid.eier)
    }

    fun oppdaterSoknadMetadataVedSendingTilFiks(fiksforsendelseId: String?, behandlingsId: String?, eier: String?) {
        log.debug("Oppdaterer soknadmetadata for behandlingsid $behandlingsId")
        val soknadMetadata = soknadMetadataRepository.hent(behandlingsId)
        soknadMetadata?.fiksForsendelseId = fiksforsendelseId
        soknadMetadataRepository.oppdater(soknadMetadata)
    }

    companion object {
        private val log by logger()
    }
}
