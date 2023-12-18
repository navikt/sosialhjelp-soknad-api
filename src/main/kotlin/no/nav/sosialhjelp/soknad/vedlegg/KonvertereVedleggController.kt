package no.nav.sosialhjelp.soknad.vedlegg

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes
import no.nav.sosialhjelp.soknad.vedlegg.konvertering.FilKonverteringService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@Unprotected
@RequestMapping("/vedlegg/konverter")
class KonvertereVedleggController(
    val filKonverteringService: FilKonverteringService
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun konverterVedlegg(
        @RequestParam("file") fil: MultipartFile,
    ): ResponseEntity<ByteArray> {
        val originaltFilnavn = fil.validate()

        val pdfBytes = filKonverteringService.konverterFilTilPdf(fil)

        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${nyttFilnavn}\"")
            .contentType(MediaType.parseMediaType(MimeTypes.APPLICATION_PDF))
            .body(pdfBytes)
    }

    private fun MultipartFile.validate(): String {
        // todo bedre exception-typer
        originalFilename?.let {
            if (it.isBlank())
                throw IllegalArgumentException("Filnavn er tomt")
        }
        if (bytes.isEmpty()) throw IllegalArgumentException("Fil er tom")

        return originalFilename ?: throw IllegalArgumentException("Filnavn er null")
    }
}
