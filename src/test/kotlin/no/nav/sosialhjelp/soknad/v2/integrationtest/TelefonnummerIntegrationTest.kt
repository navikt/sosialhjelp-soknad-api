package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataPersonRepository
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.TelefonnummerDto
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.TelefonnummerInput
import no.nav.sosialhjelp.soknad.v2.createSoknad
import no.nav.sosialhjelp.soknad.v2.opprettBrukerdataPerson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus

class TelefonnummerIntegrationTest: AbstractIntegrationTest() {

    @Autowired
    private lateinit var brukerdataPersonRepository: BrukerdataPersonRepository

    @Test
    fun `Hente telefonnummer skal returnere lagret data`() {
        val soknad = soknadRepository.save(createSoknad())
        val brukerdata = brukerdataPersonRepository.save(opprettBrukerdataPerson(soknad.id!!))

        doGet(
            "/soknad/${soknad.id}/personalia/telefonnummer",
            TelefonnummerDto::class.java
        ).also {
            assertThat(it.telefonnummerRegister).isEqualTo(soknad.eier.telefonnummer)
            assertThat(it.telefonnummerBruker).isEqualTo(brukerdata.telefonnummer)
        }
    }

    @Test
    fun `Oppdatere telefonnummer for bruker skal lagres i databasen`() {
        val soknad = soknadRepository.save(createSoknad())

        val telefonnummerInput = TelefonnummerInput(telefonnummerBruker = "32992311")

        doPut(
            "/soknad/${soknad.id}/personalia/telefonnummer",
            telefonnummerInput,
            TelefonnummerDto::class.java
        ).also {
            assertThat(it.telefonnummerBruker).isEqualTo(telefonnummerInput.telefonnummerBruker)
        }

        brukerdataPersonRepository.findByIdOrNull(soknad.id)?.let {
            assertThat(it.telefonnummer).isEqualTo(telefonnummerInput.telefonnummerBruker)
        }
    }

    @Test
    fun `Oppdatere telefonnummer med annet enn siffer gir 400 BadRequest`() {
        val soknad = soknadRepository.save(createSoknad())

        doPutExpectError(
            "/soknad/${soknad.id}/personalia/telefonnummer",
            TelefonnummerInput("asb23231"),
            HttpStatus.BAD_REQUEST
        )
    }
}
