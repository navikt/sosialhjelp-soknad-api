package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DocumentValidator(
    private val dokumentasjonRepository: DokumentasjonRepository,
    private val mellomlagerService: MellomlagerService,
) {
    fun validateDocumentsExistsInMellomlager(soknadId: UUID) {
        val filIdsMellomlager = mellomlagerService.getAllDokumenterMetadata(soknadId).map { it.filId }

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
