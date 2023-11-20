package no.nav.sosialhjelp.soknad.nymodell.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.sosialhjelp.soknad.nymodell.controller.dto.ArbeidResponse
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Arbeidsforhold
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Stillingstype
import no.nav.sosialhjelp.soknad.nymodell.service.BrukerdataService
import no.nav.sosialhjelp.soknad.nymodell.service.LivssituasjonService
import no.nav.sosialhjelp.soknad.soknad.controller.ArbeidController
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*

@WebMvcTest(ArbeidController::class)
class ArbeidControllerTest : SoknadMockMvcTest() {

    @MockkBean
    private lateinit var livssituasjonService: LivssituasjonService

    @MockkBean
    private lateinit var brukerdataService: BrukerdataService

    @Test
    fun `Arbeid finnes`() {
        every { livssituasjonService.hentArbeidsforhold(any()) } returns opprettFlereArbeidsforhold(UUID.randomUUID())
        every { brukerdataService.hentKommentarArbeidsforhold(any()) } returns "en kommentar"

        val result = mockMvc.get("/soknad/{soknadId}/arbeid", UUID.randomUUID()) {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            MockMvcResultMatchers.jsonPath("$.kommentarArbeid").value("en kommentar")
        }.andReturn()

        val adresseResponse = jacksonObjectMapper()
            .readValue(result.response.contentAsString, ArbeidResponse::class.java)

        assertThat(adresseResponse.arbeidsforhold).hasSize(4)
    }

    private fun opprettFlereArbeidsforhold(soknadId: UUID): List<Arbeidsforhold> {
        val arbeidsforholdSet: MutableList<Arbeidsforhold> = mutableListOf()
        for (i in 0..3) {
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
