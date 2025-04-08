package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.okonomi.AnnenDokumentasjonType
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class KortSoknadService(
    private val soknadService: SoknadService,
    private val dokumentasjonService: DokumentasjonService,
    private val soknadMetadataService: SoknadMetadataService,
) {
    private val logger by logger()

    @Transactional
    fun isTransitioningToKort(soknadId: UUID): Boolean {
        if (soknadMetadataService.getMetadataForSoknad(soknadId).soknadType == SoknadType.KORT) return false

        soknadMetadataService.updateSoknadType(soknadId, SoknadType.KORT)
        logger.info("Transitioning soknad $soknadId to kort")

        // Hvis en søknad skal transformeres til kort -> fjern forventet dokumentasjon og opprett obligatorisk dokumentasjon
        dokumentasjonService.resetForventetDokumentasjon(soknadId)
        dokumentasjonService.opprettObligatoriskDokumentasjon(soknadId, SoknadType.KORT)

        soknadService.updateKortSoknad(soknadId, true)

        return true
    }

    @Transactional
    fun isTransitioningToStandard(soknadId: UUID): Boolean {
        if (soknadMetadataService.getMetadataForSoknad(soknadId).soknadType == SoknadType.STANDARD) return false

        soknadMetadataService.updateSoknadType(soknadId, SoknadType.STANDARD)

        // Hvis en soknad skal transformeres til standard (igjen) -> fjern kun BEHOV og legg til SKATTEMELDING
        dokumentasjonService.fjernForventetDokumentasjon(soknadId, AnnenDokumentasjonType.BEHOV)
        dokumentasjonService.opprettDokumentasjon(soknadId, AnnenDokumentasjonType.SKATTEMELDING)

        soknadService.updateKortSoknad(soknadId, false)

        return true
    }
}
