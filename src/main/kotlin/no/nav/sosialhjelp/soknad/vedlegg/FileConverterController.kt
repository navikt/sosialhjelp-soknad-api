package no.nav.sosialhjelp.soknad.vedlegg

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes
import no.nav.sosialhjelp.soknad.vedlegg.konvertering.FileConverterService
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.slf4j.LoggerFactory
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
    private val meterRegistry: MeterRegistry
) {
    private val pdfConversionSuccess = Counter.builder("soknad_pdf_conversion_success")
    private val pdfConversionFailure = Counter.builder("soknad_pdf_conversion_failure")

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
        val upload = FileConversionUpload(file)

        log.debug("Konverterer upload {} til PDF", upload)

        val pdfBytes = try {
            virusScanner.scan(upload.bytes, upload.mimeType)
            val pdfBytes = fileConverterService.convertFileToPdf(upload.unconvertedName, upload.bytes)
            pdfConversionSuccess.tag(TAG_FILE_TYPE, "${upload.mimeType}/${upload.extension}").register(meterRegistry).increment()
            pdfBytes
        } catch (e: Exception) {
            pdfConversionFailure
                .tag(TAG_FILE_TYPE, "${upload.mimeType}/${upload.extension}")
                .tag(TAG_ERROR_CLASS, "${e::class}")
                .register(meterRegistry)
                .increment()
            throw e
        }

        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${upload.convertedName}\"")
            .contentType(MediaType.parseMediaType(MimeTypes.APPLICATION_PDF))
            .body(pdfBytes)
    }

    companion object {
        private const val TAG_FILE_TYPE = "mime_type"
        private const val TAG_ERROR_CLASS = "error_class"
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
