package no.nav.sosialhjelp.soknad.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.domene.NySoknadDto
import no.nav.sosialhjelp.soknad.domene.soknad.Soknad
import no.nav.sosialhjelp.soknad.domene.SoknadDto
import no.nav.sosialhjelp.soknad.domene.soknad.SoknadRepository
import no.nav.sosialhjelp.soknad.service.SoknadService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*

@WebMvcTest(controllers = [SoknadController::class])
@AutoConfigureMockMvc
@ActiveProfiles("no-redis", "test")
class SoknadEierInterceptorTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var soknadRepository: SoknadRepository
    @MockkBean
    private lateinit var soknadService: SoknadService

    private val soknadId = UUID.randomUUID()
    private val eier = "09044322843"

    @BeforeEach
    fun setup() {
        StaticSubjectHandlerImpl().apply {
            setUser(eier)
            SubjectHandlerUtils.setNewSubjectHandlerImpl(this)
        }
    }

    @Test
    fun `Kall til opprettSoknad er eksludert fra interceptor`() {
        every { soknadService.opprettNySoknad() } returns NySoknadDto(soknadId = soknadId)

        setUser("12345612345")
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
    fun`Soknad med riktig eier er ok`() {
        every { soknadRepository.findById(soknadId) } returns Optional.of(Soknad(soknadId, eier))
        every { soknadService.hentSoknad(soknadId) } returns SoknadDto(soknadId = soknadId)

        mockMvc
            .get("/soknad/{soknadId}", soknadId) {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                MockMvcResultMatchers.jsonPath("$.soknadId").value(soknadId)
            }
    }

    @Test
    fun `Soknad-request med feil eier blir stoppet`() {
        every { soknadRepository.findById(soknadId) } returns Optional.of(Soknad(soknadId, eier))

        setUser("12345612345")
        mockMvc
            .get("/soknad/{soknadId}", soknadId) {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isForbidden() }
                content { contentType(MediaType.APPLICATION_JSON) }
                MockMvcResultMatchers.jsonPath("$.errorMessage")
                    .value("Personen eier ikke denne soknaden.")
            }
    }

    @Test
    fun `Soknad-request med feil soknadId blir stoppet`() {
        val nySoknadId = UUID.randomUUID()
        every { soknadRepository.findById(nySoknadId) } returns Optional.of(Soknad(nySoknadId, "12345612345"))

        mockMvc
            .get("/soknad/{soknadId}", nySoknadId) {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isForbidden() }
                content { contentType(MediaType.APPLICATION_JSON) }
                MockMvcResultMatchers.jsonPath("$.errorMessage")
                    .value("Personen eier ikke denne soknaden.")
            }
    }

    @Test
    fun `Soknad som ikke finnes kaster feil`() {
        every { soknadRepository.findById(any()) } returns Optional.empty()

        mockMvc
            .get("/soknad/{soknadId}", UUID.randomUUID()) {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
                MockMvcResultMatchers.jsonPath("$.errorMessage")
                    .value("Soknaden finnes ikke.")
            }
    }

    private fun setUser(userId: String) {
        StaticSubjectHandlerImpl().apply {
            setUser(userId)
            SubjectHandlerUtils.setNewSubjectHandlerImpl(this)
        }
    }
}
