package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.okonomi.DokumentDto
import no.nav.sosialhjelp.soknad.v2.okonomi.StringToOkonomiTypeConverter
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/dokument", produces = [MediaType.APPLICATION_JSON_VALUE])
class DokumentController(
    private val dokumentService: DokumentService,
) {
    @GetMapping("/{soknadId}/{dokumentId}")
    @Operation(summary = "Henter et gitt dokument")
    @ApiResponse(
        responseCode = "200",
        description = "Dokumentet ble funnet og returneres",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                schema = Schema(type = "string", format = "binary"),
            ),
        ],
    )
    @ApiResponse(responseCode = "404", description = "Filen ble ikke funnet", content = [Content(schema = Schema(hidden = true))])
    fun getDokument(
        @PathVariable("soknadId") soknadId: UUID,
        @PathVariable("dokumentId") dokumentId: UUID,
        response: HttpServletResponse,
    ): ResponseEntity<ByteArray> {
        dokumentService.getDokument(soknadId = soknadId, dokumentId = dokumentId)
            .let { (filnavn, bytes) ->
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${filnavn}\"")
                val mimeType = FileDetectionUtils.detectMimeType(bytes)
                return ResponseEntity.ok().contentType(MediaType.parseMediaType(mimeType)).body(bytes)
            }
    }

    @PostMapping("/{soknadId}/{type}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun saveDokument(
        @PathVariable("soknadId") soknadId: UUID,
        @PathVariable("type") okonomiTypeString: String,
        @RequestParam("file") dokument: MultipartFile,
    ): DokumentDto {
        val okonomiType = StringToOkonomiTypeConverter.convert(okonomiTypeString)

        return dokumentService.saveDokument(
            soknadId = soknadId,
            type = okonomiType,
            source = dokument.bytes,
            orginaltFilnavn = dokument.originalFilename ?: error("Opplastet dokument mangler filnavn."),
        )
            .let { DokumentDto(it.dokumentId, it.filnavn) }
    }

    @DeleteMapping("/{soknadId}/{dokumentId}")
    fun deleteDokument(
        @PathVariable("soknadId") soknadId: UUID,
        @PathVariable("dokumentId") dokumentId: UUID,
    ) {
        dokumentService.deleteDokument(soknadId, dokumentId)
    }
}
