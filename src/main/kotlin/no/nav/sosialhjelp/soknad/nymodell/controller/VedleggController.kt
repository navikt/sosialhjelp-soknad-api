//package no.nav.sosialhjelp.soknad.soknad.controller
//
//import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils
//import no.nav.sosialhjelp.soknad.vedlegg.dto.FilInfoDTO
//import org.springframework.http.MediaType
//import org.springframework.web.bind.annotation.PathVariable
//import org.springframework.web.bind.annotation.PostMapping
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RequestParam
//import org.springframework.web.bind.annotation.RestController
//import org.springframework.web.multipart.MultipartFile
//
//@RestController
//@RequestMapping("/vedlegg", produces = [MediaType.APPLICATION_JSON_VALUE])
//class VedleggController(
//    private val vedleggService: VedleggService
//) {
//
//    @PostMapping("/{behandlingsId}/{type}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
//    fun behandleVedlegg(
//        @PathVariable("behandlingsId") behandlingsId: String,
//        @PathVariable("type") vedleggstype: String,
//        @RequestParam("file") fil: MultipartFile,
//    ): FilInfoDTO {
//
//        val orginaltFilnavn = fil.originalFilename ?: throw IllegalStateException("Opplastet fil mangler filnavn?")
//        val orginalData = VedleggUtils.getByteArray(fil)
//
//
//
//        return FilInfoDTO(
//            "filnavn"
//        )
//    }
//
//
//}