package no.nav.sosialhjelp.soknad.vedlegg

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import no.nav.security.token.support.core.api.Unprotected
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
    private val virusScanner: VirusScanner,
) {
    @Operation(summary = "Konverterer vedlegg til PDF")
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ApiResponse(
        responseCode = "200",
        description = "Vedlegg konvertert til PDF",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_PDF_VALUE,
                schema = Schema(type = "string", format = "binary"),
            ),
        ],
    )
    fun konverterVedlegg(
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<ByteArray> {
        val upload = FileConversionUpload(file)

        virusScanner.scan(upload.bytes)

        val pdfBytes = fileConverterService.convertFileToPdf(upload)

        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${upload.convertedName}\"")
            .contentType(MediaType.parseMediaType(MimeTypes.APPLICATION_PDF))
            .body(pdfBytes)
    }
}
