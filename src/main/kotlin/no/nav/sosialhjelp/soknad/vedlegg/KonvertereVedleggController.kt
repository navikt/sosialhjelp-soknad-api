package no.nav.sosialhjelp.soknad.vedlegg

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
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
class KonvertereVedleggController {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun konverterVedlegg(
        @RequestParam("file") fil: MultipartFile,
    ): ResponseEntity<ByteArray> {
        val orginaltFilnavn = fil.originalFilename ?: throw IllegalStateException("Opplastet fil mangler filnavn?")
        val orginalData = VedleggUtils.getByteArray(fil)
        val (filnavn, konvertertData) = VedleggUtils.konverterFilHvisStottet(orginaltFilnavn, orginalData)

        val mimeType = FileDetectionUtils.detectMimeType(konvertertData)
        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${filnavn}\"")
            .contentType(MediaType.parseMediaType(mimeType))
            .body(konvertertData)
    }
}