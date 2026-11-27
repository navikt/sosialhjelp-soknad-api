package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/dokument", produces = [MediaType.APPLICATION_JSON_VALUE])
class DokumentController(
    private val mellomlagerService: MellomlagerService,
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
        return run { mellomlagerService.getDokument(soknadId, dokumentId) }
            .let { mellomlagretDokument ->
                require(mellomlagretDokument.data != null) { "Fant ikke data for dokument $dokumentId" }

                response.setHeader(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"${mellomlagretDokument.filnavn}\"",
                )
                ResponseEntity.ok()
                    .contentType(mellomlagretDokument.data.toMediaType())
                    .body(mellomlagretDokument.data)
            }
    }
}

private fun ByteArray.toMediaType(): MediaType =
    FileDetectionUtils.detectMimeType(this).let { MediaType.parseMediaType(it) }
