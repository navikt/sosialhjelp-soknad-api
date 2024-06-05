package no.nav.sosialhjelp.soknad.vedlegg

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.vedlegg.dto.DokumentUpload
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
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

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/opplastetVedlegg", produces = [MediaType.APPLICATION_JSON_VALUE])
class OpplastetVedleggRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val mellomlagringService: MellomlagringService,
) {
    @GetMapping("/{behandlingsId}/{dokumentId}/fil")
    @Operation(summary = "Hent innhold i dokument")
    @ApiResponse(
        responseCode = "200",
        description = "Dokumentets innhold sendes til klienten",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                schema = Schema(type = "string", format = "binary"),
            ),
        ],
    )
    fun getDokument(
        @PathVariable("behandlingsId") behandlingsId: String,
        @PathVariable("dokumentId") dokumentId: String,
        response: HttpServletResponse,
    ): ResponseEntity<ByteArray> {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        log.info("Forsøker å hente dokument $dokumentId fra mellomlagring hos KS")

        val dokument = mellomlagringService.getVedlegg(behandlingsId, dokumentId)

        if (dokument == null) {
            log.error("Fant ikke dokument $dokumentId hos KS")
            throw IkkeFunnetException("Fant ikke vedlegg $dokumentId")
        }

        val contentType = MediaType.parseMediaType(detectMimeType(dokument.data))
        val contentDisposition = "attachment; filename=\"${dokument.filnavn}\""
        log.info("Fant dokument $dokumentId hos KS")

        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
            .contentType(contentType)
            .body(dokument.data)
    }

    @PostMapping("/{behandlingsId}/{dokumentasjonType}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadDokument(
        @PathVariable("behandlingsId") behandlingsId: String,
        @PathVariable("dokumentasjonType") dokumentasjonType: String,
        @RequestParam("file") fil: MultipartFile,
    ): DokumentUpload {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        val orginaltFilnavn = fil.originalFilename ?: throw IllegalStateException("Opplastet dokument mangler filnavn?")
        val orginalData = VedleggUtils.getByteArray(fil)

        return mellomlagringService
            .uploadVedlegg(behandlingsId, dokumentasjonType, orginalData, orginaltFilnavn)
            .let { DokumentUpload.fromMellomlagretVedleggMetadata(it) }
    }

    @DeleteMapping("/{behandlingsId}/{dokumentId}")
    fun deleteDokument(
        @PathVariable("behandlingsId") behandlingsId: String,
        @PathVariable("dokumentId") dokumentId: String,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        log.info("Sletter dokument $dokumentId fra KS mellomlagring")
        mellomlagringService.deleteVedleggAndUpdateVedleggstatus(behandlingsId, dokumentId)
    }

    companion object {
        private val log by logger()
    }
}
