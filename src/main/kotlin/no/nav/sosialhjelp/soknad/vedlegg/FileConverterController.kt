package no.nav.sosialhjelp.soknad.vedlegg

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes
import no.nav.sosialhjelp.soknad.vedlegg.konvertering.FileConverterService
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
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
class FileConverterController(
    private val fileConverterService: FileConverterService,
    private val virusScanner: VirusScanner
) {
    @Operation(summary = "Konverterer vedlegg til PDF")
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ApiResponse(
        responseCode = "200", description = "Vedlegg konvertert til PDF",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_PDF_VALUE,
                schema = Schema(type = "string", format = "binary")
            )
        ]
    )
    fun konverterVedlegg(
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<ByteArray> {
        val filenameNotNull = file.validateFileName()
        virusScanner.scan(filenameNotNull, file.bytes, "**KONVERTERING**", detectMimeType(file.bytes))

        val (convertedName, pdfBytes) = fileConverterService.convertFileToPdf(filenameNotNull, file.bytes)

        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${convertedName}\"")
            .contentType(MediaType.parseMediaType(MimeTypes.APPLICATION_PDF))
            .body(pdfBytes)
    }

    private fun MultipartFile.validateFileName(): String {
        return originalFilename?.also {
            if (it.isBlank() || it.isEmpty())
                throw IllegalStateException("Filnavn er tomt")
        }
            ?: throw IllegalStateException("Filnavn er null")
    }
}
