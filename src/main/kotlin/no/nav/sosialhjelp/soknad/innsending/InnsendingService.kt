package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime

@Component
open class InnsendingService(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val opplastetVedleggRepository: OpplastetVedleggRepository,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val clock: Clock
) {
    open fun oppdaterSoknadUnderArbeid(soknadUnderArbeid: SoknadUnderArbeid?) {
        check(soknadUnderArbeid != null) { "Kan ikke sende søknad som ikke finnes eller som mangler søknadsid" }
        soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(soknadUnderArbeid)
        soknadUnderArbeid.status = SoknadUnderArbeidStatus.LAAST
        soknadUnderArbeidRepository.oppdaterInnsendingStatus(soknadUnderArbeid, soknadUnderArbeid.eier)
    }

    open fun finnOgSlettSoknadUnderArbeidVedSendingTilFiks(behandlingsId: String, eier: String) {
        logger.debug("Henter søknad under arbeid for behandlingsid $behandlingsId og eier $eier")
        soknadUnderArbeidRepository.hentSoknadNullable(behandlingsId, eier)
            ?.let { soknadUnderArbeidRepository.slettSoknad(it, eier) }
    }

    open fun oppdaterSoknadMetadataVedSendingTilFiks(fiksforsendelseId: String?, behandlingsId: String?, eier: String?) {
        logger.debug("Oppdaterer soknadmetadata for behandlingsid $behandlingsId og eier $eier")
        val soknadMetadata = soknadMetadataRepository.hent(behandlingsId)
        soknadMetadata?.fiksForsendelseId = fiksforsendelseId
        soknadMetadata?.innsendtDato = LocalDateTime.now(clock)
        soknadMetadataRepository.oppdater(soknadMetadata)
    }

    open fun hentSoknadMetadata(behandlingsId: String, eier: String?): SoknadMetadata {
        return soknadMetadataRepository.hent(behandlingsId)
            ?: throw RuntimeException("Finner ikke sendt søknad med behandlingsId $behandlingsId")
    }

    open fun hentSoknadUnderArbeid(behandlingsId: String, eier: String): SoknadUnderArbeid {
        return soknadUnderArbeidRepository.hentSoknadNullable(behandlingsId, eier)
            ?: throw RuntimeException("Finner ikke sendt søknad med behandlingsId $behandlingsId")
    }

    open fun hentAlleOpplastedeVedleggForSoknad(soknadUnderArbeid: SoknadUnderArbeid): List<OpplastetVedlegg> {
        return opplastetVedleggRepository.hentVedleggForSoknad(soknadUnderArbeid.soknadId, soknadUnderArbeid.eier)
    }

    open fun finnFiksForsendelseIdForEttersendelse(soknadUnderArbeid: SoknadUnderArbeid): String? {
        val tilknyttetBehandlingsId = soknadUnderArbeid.tilknyttetBehandlingsId
            ?: throw IllegalStateException("TilknyttetBehandlingsId kan ikke være null for en ettersendelse")

        return soknadMetadataRepository.hent(tilknyttetBehandlingsId)?.fiksForsendelseId
            ?: throw IllegalStateException("Finner ikke søknaden det skal ettersendes på")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(InnsendingService::class.java)
    }
}
