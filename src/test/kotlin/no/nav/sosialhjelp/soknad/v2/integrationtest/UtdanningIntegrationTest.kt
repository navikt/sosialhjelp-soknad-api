package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.livssituasjon.IkkeStudentInput
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRepository
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Studentgrad
import no.nav.sosialhjelp.soknad.v2.livssituasjon.StudentgradInput
import no.nav.sosialhjelp.soknad.v2.livssituasjon.UtdanningDto
import no.nav.sosialhjelp.soknad.v2.opprettLivssituasjon
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class UtdanningIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var livssituasjonRepository: LivssituasjonRepository

    @Test
    fun `Hente utdanning skal returnere riktig data`() {
        val soknad = soknadRepository.save(opprettSoknad())
        val brukerdata = livssituasjonRepository.save(opprettLivssituasjon(soknad.id))

        doGet(
            "/soknad/${soknad.id}/utdanning",
            UtdanningDto::class.java
        ).also {
            assertThat(it.erStudent).isEqualTo(brukerdata.utdanning.erStudent)
            assertThat(it.studentgrad).isEqualTo(brukerdata.utdanning.studentgrad)
        }
    }

    @Test
    fun `Sette ikke student skal lagres`() {
        val soknad = soknadRepository.save(opprettSoknad())

        val input = IkkeStudentInput()

        doPut(
            "/soknad/${soknad.id}/utdanning",
            input,
            UtdanningDto::class.java
        )

        livssituasjonRepository.findByIdOrNull(soknad.id)?.let {
            assertThat(it.utdanning.erStudent).isFalse()
            assertThat(it.utdanning.studentgrad).isNull()
        }
            ?: fail("Utdanning finnes ikke")
    }

    @Test
    fun `Sette studentgrad skal lagres`() {
        val soknad = soknadRepository.save(opprettSoknad())

        val input = StudentgradInput(studentgrad = Studentgrad.HELTID)

        doPut(
            "/soknad/${soknad.id}/utdanning",
            input,
            UtdanningDto::class.java
        )

        livssituasjonRepository.findByIdOrNull(soknad.id)?.let {
            assertThat(it.utdanning.erStudent).isTrue()
            assertThat(it.utdanning.studentgrad).isEqualTo(input.studentgrad)
        }
            ?: fail("Utdanning finnes ikke")
    }
}
