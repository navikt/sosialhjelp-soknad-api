package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.v2.okonomi.DokumentDto
import no.nav.sosialhjelp.soknad.v2.okonomi.StringToOpplysningTypeConverter
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
    private val dokumentlagerService: DokumentlagerService,
    private val dokumentRefService: DokumentRefService,
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
        return when (dokumentRefService.getRef(soknadId, dokumentId)) {
            null -> {
                dokumentlagerService.deleteDokument(soknadId, dokumentId)
                throw IkkeFunnetException("Fant ikke dokumentreferanse $dokumentId")
            }
            else -> {
                runCatching { dokumentlagerService.getDokument(soknadId, dokumentId) }
                    .onFailure { dokumentRefService.removeRef(soknadId, dokumentId) }
                    .getOrThrow()
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
    }

    @PostMapping("/{soknadId}/{type}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun saveDokument(
        @PathVariable("soknadId") soknadId: UUID,
        @PathVariable("type") opplysningTypeString: String,
        @RequestParam("file") dokument: MultipartFile,
    ): DokumentDto {
        val opplysningType = StringToOpplysningTypeConverter.convert(opplysningTypeString)

        return runCatching {
            dokument.originalFilename?.let { dokumentlagerService.uploadDokument(soknadId, dokument.bytes, it) }
                ?: throw IllegalArgumentException("Opplastet dokument mangler filnavn.")
        }
            .onSuccess { mellomlagretDokument ->
                dokumentRefService.addRef(
                    soknadId = soknadId,
                    type = opplysningType,
                    fiksFilId = mellomlagretDokument.filId.toUuid(),
                    filnavn = mellomlagretDokument.filnavn,
                )
            }
            .getOrThrow()
            .let { DokumentDto(it.filId.toUuid(), it.filnavn) }
    }

    @DeleteMapping("/{soknadId}/{dokumentId}")
    fun deleteDokument(
        @PathVariable("soknadId") soknadId: UUID,
        @PathVariable("dokumentId") dokumentId: UUID,
    ) {
        runCatching { dokumentlagerService.deleteDokument(soknadId, dokumentId) }
            .onSuccess { dokumentRefService.removeRef(soknadId, dokumentId) }
            .onFailure { throw IllegalStateException("Feil ved sletting av dokument $dokumentId", it) }
    }
}

private fun String.toUuid() = UUID.fromString(this)

private fun ByteArray.toMediaType(): MediaType =
    FileDetectionUtils.detectMimeType(this).let { MediaType.parseMediaType(it) }
