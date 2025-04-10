package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus.FORVENTET
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus.LEVERT_TIDLIGERE
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DokumentasjonStatusUseCaseHandler(
    private val dokumentasjonService: DokumentasjonService,
    private val dokumentlagerService: DokumentlagerService,
) {
    fun findForventetDokumentasjon(soknadId: UUID): List<Dokumentasjon> =
        dokumentasjonService.findDokumentasjonForSoknad(soknadId)

    fun updateDokumentasjonStatus(
        soknadId: UUID,
        type: OpplysningType,
        hasLevert: Boolean,
    ) {
        val dokumentasjon =
            dokumentasjonService.findDokumentasjonByType(soknadId, type)
                ?: error("Dokumentasjon finnes ikke for type: $type")

        when {
            dokumentasjon.doesntNeedUpdate(hasLevert) -> return
            hasLevert -> handleHasLevert(dokumentasjon)
            !hasLevert -> dokumentasjonService.updateDokumentasjon(dokumentasjon.copy(status = FORVENTET))
        }
    }

    private fun handleHasLevert(dokumentasjon: Dokumentasjon) {
        dokumentasjon.copy(status = LEVERT_TIDLIGERE, dokumenter = emptySet())
            .also { dokumentasjonService.updateDokumentasjon(it) }

        dokumentasjon.dokumenter
            .map { it.dokumentId }
            .forEach { dokumentlagerService.deleteDokument(dokumentasjon.soknadId, it) }
    }
}

private fun Dokumentasjon.doesntNeedUpdate(hasLevert: Boolean): Boolean {
    return when {
        status == LEVERT_TIDLIGERE && hasLevert -> true
        status != LEVERT_TIDLIGERE && !hasLevert -> true
        else -> false
    }
}
