//package no.nav.sosialhjelp.soknad.nymodell.controller
//
//import com.ninjasquad.springmockk.MockkBean
//import io.mockk.every
//import no.nav.sosialhjelp.soknad.nymodell.controller.dto.BosituasjonDto
//import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.type.Botype
//import no.nav.sosialhjelp.soknad.nymodell.service.LivssituasjonService
//import org.junit.jupiter.api.Test
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
//import org.springframework.http.MediaType
//import org.springframework.test.web.servlet.get
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
//import java.util.*
//
//@WebMvcTest(controllers = [BosituasjonController::class])
//class BosituasjonControllerTest: SoknadMockMvcTest() {
//
//    @MockkBean
//    private lateinit var service: LivssituasjonService
//
//    @Test
//    fun `Bosituasjon finnes`() {
//        BosituasjonDto(botype = Botype.EIER, antallPersoner = 3)
//            .toBosituasjon(soknadId = UUID.randomUUID())
//            .also { every { service.hentBosituasjon(any()) } returns it }
//
//        mockMvc.get("/soknad/{soknadId}/bosituasjon", UUID.randomUUID()) {
//            contentType = MediaType.APPLICATION_JSON
//            accept = MediaType.APPLICATION_JSON
//        }.andExpect {
//            status { isOk() }
//            content { contentType(MediaType.APPLICATION_JSON) }
//            jsonPath("$.botype").value("eier")
//            jsonPath("$.antallPersoner").value(3)
//        }
//    }
//
//    @Test
//    fun `Bosituasjon finnes ikke`() {
//        every { service.hentBosituasjon(any()) } returns null
//
//        mockMvc.get("/soknad/{soknadId}/bosituasjon", UUID.randomUUID()) {
//            contentType = MediaType.APPLICATION_JSON
//            accept = MediaType.APPLICATION_JSON
//        }.andExpect {
//            status { isNotFound() }
//            content { contentType(MediaType.APPLICATION_JSON) }
//            jsonPath("$.message").value("Bosituasjon finnes ikke.")
//        }
//    }
//
//    @Test
//    fun `Hent Bosituasjon - SoknadId er ikke en UUID`() {
//        mockMvc.get("/soknad/{soknadId}/bosituasjon",1234) {
//            contentType = MediaType.APPLICATION_JSON
//            accept = MediaType.APPLICATION_JSON
//        }.andExpect {
//            status { is5xxServerError() }
//            content { contentType(MediaType.APPLICATION_JSON) }
//            jsonPath("$.id").value("unexpected_error")
//            jsonPath("$.message").value("Noe uventet feilet")
//        }
//    }
//}
