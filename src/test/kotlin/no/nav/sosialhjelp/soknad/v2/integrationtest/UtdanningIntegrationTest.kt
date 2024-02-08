package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataFormeltRepository
import no.nav.sosialhjelp.soknad.v2.brukerdata.Studentgrad
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.UtdanningDto
import no.nav.sosialhjelp.soknad.v2.createSoknad
import no.nav.sosialhjelp.soknad.v2.opprettBrukerdataFormelt
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import java.util.*

class UtdanningIntegrationTest: AbstractIntegrationTest() {

    @Autowired
    private lateinit var brukerdataFormeltRepository: BrukerdataFormeltRepository

    @Test
    fun `Hente utdanning skal returnere riktig data`() {
        val soknad = soknadRepository.save(createSoknad())
        val brukerdata = brukerdataFormeltRepository.save(opprettBrukerdataFormelt(soknad.id!!))

        doGet(
            "/soknad/${soknad.id}/utdanning",
            UtdanningDto::class.java
        ).also {
            assertThat(it.erStudent).isEqualTo(brukerdata.utdanning?.erStudent)
            assertThat(it.studentgrad).isEqualTo(brukerdata.utdanning?.studentGrad)
        }
    }

    @Test
    fun `Oppdatere utdanning skal lagres i databasen`() {
        val soknad = soknadRepository.save(createSoknad())

        val utdanningInput = UtdanningDto(
            erStudent = true,
            studentgrad = Studentgrad.DELTID
        )

        doPut(
            "/soknad/${soknad.id}/utdanning",
            utdanningInput,
            UtdanningDto::class.java
        )

        brukerdataFormeltRepository.findByIdOrNull(soknad.id)?.let {
            assertThat(it.utdanning?.erStudent).isEqualTo(utdanningInput.erStudent)
            assertThat(it.utdanning?.studentGrad).isEqualTo(utdanningInput.studentgrad)
        }
            ?: fail("Utdanning finnes ikke")
    }

    @Test
    fun `erStudent = false og studentgrad != null skal gi feil`() {
        doPutExpectError(
            "/soknad/${UUID.randomUUID()}/utdanning",
            UtdanningDto(
                erStudent = false,
                studentgrad = Studentgrad.DELTID
            ),
            HttpStatus.BAD_REQUEST
        )
    }
}
