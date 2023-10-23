package no.nav.sosialhjelp.soknad.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.sosialhjelp.soknad.domene.NySoknadDto
import no.nav.sosialhjelp.soknad.domene.SoknadDto
import no.nav.sosialhjelp.soknad.service.SoknadService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*

@WebMvcTest(controllers = [SoknadController::class])
class SoknadControllerTest: SoknadMockMvcTest() {

    @MockkBean
    private lateinit var soknadService: SoknadService

    private val soknadId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
    }

    @Test
    fun `Opprett ny soknad`() {
        every { soknadService.opprettNySoknad() } returns NySoknadDto(soknadId)

        mockMvc
            .post("/soknad/opprettSoknad") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                MockMvcResultMatchers.jsonPath("$.soknadId").value(soknadId)
            }
    }

    @Test
    fun `Hent soknad`() {
        every { soknadService.hentSoknad(soknadId) } returns SoknadDto(soknadId)

        mockMvc
            .get("/soknad/{soknadId}", soknadId) {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                MockMvcResultMatchers.jsonPath("$.soknadId").value(soknadId)
                MockMvcResultMatchers.jsonPath("$.hvorforSoke").value("Fordi")
            }
    }
}
