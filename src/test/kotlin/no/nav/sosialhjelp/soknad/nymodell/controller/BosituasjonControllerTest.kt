package no.nav.sosialhjelp.soknad.nymodell.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.sosialhjelp.soknad.nymodell.controller.dto.BosituasjonDto
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Bosituasjon
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Botype
import no.nav.sosialhjelp.soknad.nymodell.service.LivssituasjonService
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.util.*

@WebMvcTest(controllers = [BosituasjonController::class])
class BosituasjonControllerTest : SoknadMockMvcTest() {

    @MockkBean
    private lateinit var service: LivssituasjonService

    @Test
    fun `Bosituasjon finnes`() {
        BosituasjonDto(botype = Botype.EIER, antallPersoner = 3)
            .let { Bosituasjon(UUID.randomUUID(), it.botype, it.antallPersoner) }
            .also { every { service.hentBosituasjon(any()) } returns it }

        mockMvc.get("/soknad/{soknadId}/bosituasjon", UUID.randomUUID()) {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.botype").value("eier")
            jsonPath("$.antallPersoner").value(3)
        }
    }

    @Test
    fun `Bosituasjon finnes ikke`() {
        every { service.hentBosituasjon(any()) } returns null

        mockMvc.get("/soknad/{soknadId}/bosituasjon", UUID.randomUUID()) {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.message").value("Bosituasjon finnes ikke.")
        }
    }
}
