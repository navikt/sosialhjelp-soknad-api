package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.TelefonnummerDto
import no.nav.sosialhjelp.soknad.v2.kontakt.TelefonnummerInput
import no.nav.sosialhjelp.soknad.v2.opprettKontakt
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus

class TelefonnummerIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var kontaktRepository: KontaktRepository

    @Test
    fun `Hente telefonnummer skal returnere lagret data`() {
        val soknad = soknadRepository.save(opprettSoknad())
        val kontakt = kontaktRepository.save(opprettKontakt(soknad.id))

        doGet(
            "/soknad/${soknad.id}/personalia/telefonnummer",
            TelefonnummerDto::class.java
        ).also {
            assertThat(it.telefonnummerRegister).isEqualTo(kontakt.telefonnummer.fraRegister)
            assertThat(it.telefonnummerBruker).isEqualTo(kontakt.telefonnummer.fraBruker)
        }
    }

    @Test
    fun `Oppdatere telefonnummer for bruker skal lagres i databasen`() {
        val soknad = soknadRepository.save(opprettSoknad())

        val telefonnummerInput = TelefonnummerInput(telefonnummerBruker = "32992311")

        doPut(
            "/soknad/${soknad.id}/personalia/telefonnummer",
            telefonnummerInput,
            TelefonnummerDto::class.java
        ).also {
            assertThat(it.telefonnummerBruker).isEqualTo(telefonnummerInput.telefonnummerBruker)
        }

        kontaktRepository.findByIdOrNull(soknad.id)?.let {
            assertThat(it.telefonnummer.fraBruker).isEqualTo(telefonnummerInput.telefonnummerBruker)
        }
    }

    @Test
    fun `Oppdatere telefonnummer med annet enn siffer gir 400 BadRequest`() {
        val soknad = soknadRepository.save(opprettSoknad())

        doPutExpectError(
            "/soknad/${soknad.id}/personalia/telefonnummer",
            TelefonnummerInput("asb23231"),
            HttpStatus.BAD_REQUEST
        )
    }
}
