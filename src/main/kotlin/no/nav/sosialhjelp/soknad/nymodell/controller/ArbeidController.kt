//package no.nav.sosialhjelp.soknad.soknad.controller
//
//import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
//import no.nav.sosialhjelp.soknad.nymodell.controller.dto.ArbeidDto
//import no.nav.sosialhjelp.soknad.nymodell.controller.dto.ArbeidsforholdDto
//import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Arbeidsforhold
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
//@RequestMapping("/soknad/{soknadId}/arbeid", produces = [MediaType.APPLICATION_JSON_VALUE])
//class ArbeidController(
////    private val service: LivssituasjonService
//) {
//
//    @GetMapping
//    fun hentArbeid(
//        @PathVariable("soknadId") soknadId: UUID
//    ): ArbeidDto {
//        return service.hentArbeid(soknadId)?.toDto()
//            ?: throw IkkeFunnetException(melding = "Fant ikke Arbeid")
//    }
//
//    @PutMapping
//    fun updateArbeid(
//        @PathVariable("soknadId") soknadId: UUID,
//        @RequestBody arbeidDto: ArbeidDto
//    ): ArbeidDto {
//        return service.updateArbeid(
//            soknadId = soknadId,
//            kommentarArbeid = arbeidDto.kommentarArbeid
//        ).toDto()
//    }
//}
//
////fun Arbeid.toDto(): ArbeidDto = ArbeidDto (
////    kommentarArbeid = kommentarArbeid,
////    arbeidsforhold = arbeidsforhold.map {
////        it.toDto()
////    }.toSet()
////)
//
//fun Arbeidsforhold.toDto() = ArbeidsforholdDto (
//    arbeidsgivernavn = arbeidsgivernavn,
//    orgnummer = orgnummer,
//    fraOgMed = fraOgMed.toString(),
//    tilOgMed = tilOgMed.toString(),
//    stillingstype = stillingstype,
//    stillingsprosent = stillingsprosent
//)
