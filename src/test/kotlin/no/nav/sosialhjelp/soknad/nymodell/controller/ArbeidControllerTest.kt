//package no.nav.sosialhjelp.soknad.nymodell.controller
//
//import com.ninjasquad.springmockk.MockkBean
//import io.mockk.every
//import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Arbeid
//import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Arbeidsforhold
//import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.type.Stillingstype
//import no.nav.sosialhjelp.soknad.nymodell.service.LivssituasjonService
//import no.nav.sosialhjelp.soknad.soknad.controller.ArbeidController
//import org.junit.jupiter.api.Test
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
//import java.util.*
//
//@WebMvcTest(ArbeidController::class)
//class ArbeidControllerTest: SoknadMockMvcTest() {
//
//    @MockkBean
//    private lateinit var service: LivssituasjonService
//
//    @Test
//    fun `Arbeid finnes`() {
//        val arbeid = Arbeid(
//            id = UUID.randomUUID(),
//            kommentarArbeid = "kommentar",
//            arbeidsforhold = opprettFlereArbeidsforhold(UUID.randomUUID()))
//
//        every { service.hentArbeid(any()) } returns arbeid
//
//    }
//
//    private fun opprettFlereArbeidsforhold(soknadId: UUID): Set<Arbeidsforhold> {
//        val arbeidsforholdSet: MutableSet<Arbeidsforhold> = mutableSetOf()
//        for(i in 0..3) {
//            arbeidsforholdSet.add(opprettArbeidsforhold(soknadId, "Test-firma$i"))
//        }
//        return arbeidsforholdSet
//    }
//    private fun opprettArbeidsforhold(arbeidId: UUID, navn: String = "Test-firma"): Arbeidsforhold {
//        return Arbeidsforhold(
//            soknadId = arbeidId,
//            arbeidsgivernavn = navn,
//            stillingstype = Stillingstype.FAST
//        )
//    }
//}