package no.nav.sosialhjelp.soknad.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon.Botype.EIER
import no.nav.sosialhjelp.soknad.model.Bosituasjon
import no.nav.sosialhjelp.soknad.service.BosituasjonService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.util.*

@WebMvcTest(BosituasjonController::class)
@AutoConfigureMockMvc
@ActiveProfiles("no-redis", "test")
class BosituasjonControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var bosituasjonService: BosituasjonService

    @Test
    fun `Bosituasjon finnes`() {
        val bosituasjon = Bosituasjon(
//            id = 1L,
            soknadId = UUID.randomUUID(),
            botype = EIER,
            antallPersoner = 3
        )
        every { bosituasjonService.hentBosituasjon(any()) } returns bosituasjon

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
        every { bosituasjonService.hentBosituasjon(any()) } returns null

        mockMvc.get("/soknad/{soknadId}/bosituasjon", UUID.randomUUID()) {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.message").value("Bosituasjon finnes ikke.")
        }
    }

    @Test
    fun `Hent Bosituasjon - SoknadId er ikke en UUID`() {
        mockMvc.get("/soknad/{soknadId}/bosituasjon",1234) {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { is5xxServerError() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id").value("unexpected_error")
            jsonPath("$.message").value("Noe uventet feilet")
        }
    }
}
