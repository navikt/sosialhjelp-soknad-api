package no.nav.sosialhjelp.soknad.vedlegg

import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentController
import no.nav.sosialhjelp.soknad.vedlegg.dto.DokumentUpload
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Component
class DokumentProxy(private val dokumentController: DokumentController) {
    fun getDokument(
        soknadId: String,
        dokumentId: String,
        response: HttpServletResponse,
    ): ResponseEntity<ByteArray> {
        return dokumentController.getDokument(UUID.fromString(soknadId), UUID.fromString(dokumentId), response)
    }

    fun uploadDocument(
        soknadId: String,
        vedleggTypeString: String,
        fil: MultipartFile,
    ): DokumentUpload {
        return dokumentController.saveDokument(
            soknadId = UUID.fromString(soknadId),
            opplysningTypeString = mapToOpplysningTypeString(vedleggTypeString),
            dokument = fil,
        )
            .let { dto ->
                DokumentUpload(
                    filename = dto.filnavn,
                    dokumentId = dto.dokumentId.toString(),
                )
            }
    }

    fun deleteDocument(
        soknadId: String,
        dokumentId: String,
    ) {
        dokumentController.deleteDokument(UUID.fromString(soknadId), UUID.fromString(dokumentId))
    }
}

private fun mapToOpplysningTypeString(vedleggTypeString: String): String {
    return VedleggType.entries.find { it.stringName == vedleggTypeString }
        ?.let { it.opplysningType?.name }
        ?: error("Kunne ikke mappe til OpplysningType: $vedleggTypeString")
}
