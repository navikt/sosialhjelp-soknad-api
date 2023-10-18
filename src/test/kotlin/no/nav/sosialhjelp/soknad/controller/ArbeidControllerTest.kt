package no.nav.sosialhjelp.soknad.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.sosialhjelp.soknad.model.Arbeid
import no.nav.sosialhjelp.soknad.model.Arbeidsforhold
import no.nav.sosialhjelp.soknad.model.Stillingstype
import no.nav.sosialhjelp.soknad.service.ArbeidService
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import java.util.*

@WebMvcTest(ArbeidController::class)
class ArbeidControllerTest: SoknadMockMvcTest() {

    @MockkBean
    private lateinit var arbeidService: ArbeidService

    @Test
    fun `Arbeid finnes`() {
        val arbeid = Arbeid(
            soknadId = UUID.randomUUID(),
            kommentarArbeid = "kommentar",
            arbeidsforhold = opprettFlereArbeidsforhold(UUID.randomUUID()))

        every { arbeidService.hentArbeid(any()) } returns arbeid

    }

    private fun opprettFlereArbeidsforhold(soknadId: UUID): Set<Arbeidsforhold> {
        val arbeidsforholdSet: MutableSet<Arbeidsforhold> = mutableSetOf()
        for(i in 0..3) {
            arbeidsforholdSet.add(opprettArbeidsforhold(soknadId, "Test-firma$i"))
        }
        return arbeidsforholdSet
    }
    private fun opprettArbeidsforhold(arbeidId: UUID, navn: String = "Test-firma"): Arbeidsforhold {
        return Arbeidsforhold(
            soknadId = arbeidId,
            arbeidsgivernavn = navn,
            stillingstype = Stillingstype.FAST
        )
    }
}