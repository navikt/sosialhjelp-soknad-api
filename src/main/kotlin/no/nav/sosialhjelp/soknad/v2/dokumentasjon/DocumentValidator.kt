package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DocumentValidator(
    private val mellomlagringService: MellomlagringService,
    private val dokumentasjonRepository: DokumentasjonRepository,
) {
    fun validateDocumentsExistsInMellomlager(soknadId: UUID) {
        val filIdsMellomlager = mellomlagringService.getAllVedlegg(soknadId).map { metadata -> metadata.filId }

        runCatching {
            dokumentasjonRepository.findAllBySoknadId(soknadId)
                .forEach { dokumentasjon ->
                    dokumentasjon.dokumenter.forEach { dokument ->
                        when (filIdsMellomlager.find { it == dokument.dokumentId.toString() }) {
                            null -> {
                                logger.error(
                                    "Dokument(${dokument.dokumentId}) på dokumentasjon(type=${dokumentasjon.type}) " +
                                        "mangler i FIKS mellomlager. Sletter.",
                                )
                                dokumentasjonRepository.removeDokumentFromDokumentasjon(soknadId, dokument.dokumentId)
                            }
                        }
                    }
                }
        }
            .onFailure {
                throw IllegalStateException("Feil ved validering av dokumenter hos mellomlager", it)
            }
    }

    companion object {
        private val logger by logger()
    }
}
