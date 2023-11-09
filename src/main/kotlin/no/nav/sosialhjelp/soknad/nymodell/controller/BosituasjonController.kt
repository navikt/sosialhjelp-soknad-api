//package no.nav.sosialhjelp.soknad.nymodell.controller
//
//import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
//import no.nav.sosialhjelp.soknad.nymodell.controller.dto.BosituasjonDto
//import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Bosituasjon
////import no.nav.sosialhjelp.soknad.nymodell.service.LivssituasjonService
//import org.springframework.http.MediaType
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.PathVariable
//import org.springframework.web.bind.annotation.PutMapping
//import org.springframework.web.bind.annotation.RequestBody
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//import java.util.*
//
//@RestController
//@RequestMapping("/soknad/{soknadId}/bosituasjon", produces = [MediaType.APPLICATION_JSON_VALUE])
//class BosituasjonController(
////    private val service: LivssituasjonService
//) {
//    @GetMapping
//    fun hentBosituasjon(
//        @PathVariable("soknadId") soknadIdString: String,
//    ): BosituasjonDto {
//        val soknadId = UUID.fromString(soknadIdString)
//        return service.hentBosituasjon(soknadId)?.toDto()
//            ?: throw IkkeFunnetException("Bosituasjon finnes ikke.")
//    }
//
//    @PutMapping
//    fun oppdaterBosituasjon(
//        @PathVariable("soknadId") soknadIdString: String?,
//        @RequestBody bosituasjonDto: BosituasjonDto
//    ) {
//        val bosituasjon = bosituasjonDto.toBosituasjon(UUID.fromString(soknadIdString))
//        service.oppdaterBosituasjon(bosituasjon)
//    }
//}
//
//fun BosituasjonDto.toBosituasjon(soknadId: UUID): Bosituasjon {
//    return Bosituasjon(
//        soknadId = soknadId,
//        botype = botype,
//        antallPersoner = antallPersoner
//    )
//}
//
//fun Bosituasjon.toDto() = BosituasjonDto(
//    botype = botype,
//    antallPersoner = antallPersoner
//)
