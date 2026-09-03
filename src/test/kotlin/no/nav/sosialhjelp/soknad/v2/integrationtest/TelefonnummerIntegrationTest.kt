package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.app.exceptions.SoknadApiErrorType
import no.nav.sosialhjelp.soknad.v2.kontakt.TelefonnummerDto
import no.nav.sosialhjelp.soknad.v2.kontakt.TelefonnummerInput
import no.nav.sosialhjelp.soknad.v2.opprettKontakt
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus

class TelefonnummerIntegrationTest : AbstractIntegrationTest() {
    @Test
    fun `Hente telefonnummer skal returnere lagret data`() {
        val soknad = soknadRepository.save(opprettSoknad(id = soknadId))
        val kontakt = kontaktRepository.save(opprettKontakt(soknad.id))

        doGet(
            "/soknad/${soknad.id}/personalia/telefonnummer",
            TelefonnummerDto::class.java,
        ).also {
            assertThat(it.telefonnummerRegister).isEqualTo(kontakt.telefonnummer.fraRegister)
            assertThat(it.telefonnummerBruker).isEqualTo(kontakt.telefonnummer.fraBruker)
        }
    }

    @Test
    fun `Oppdatere telefonnummer for bruker skal lagres i databasen`() {
        val soknad = soknadRepository.save(opprettSoknad(id = soknadId))

        val telefonnummerInput = TelefonnummerInput(telefonnummerBruker = "32992311")

        doPut(
            "/soknad/${soknad.id}/personalia/telefonnummer",
            telefonnummerInput,
            TelefonnummerDto::class.java,
        ).also {
            assertThat(it.telefonnummerBruker).isEqualTo("+47${telefonnummerInput.telefonnummerBruker}")
        }

        kontaktRepository.findByIdOrNull(soknad.id)?.let {
            assertThat(it.telefonnummer.fraBruker).isEqualTo("+47${telefonnummerInput.telefonnummerBruker}")
        }
    }

    @Test
    fun `Oppdatere telefonnummer med landkode lagres i databasen`() {
        val soknad = soknadRepository.save(opprettSoknad(id = soknadId))

        val telefonnummerInput = TelefonnummerInput(telefonnummerBruker = "+4732992311")

        doPut(
            "/soknad/${soknad.id}/personalia/telefonnummer",
            telefonnummerInput,
            TelefonnummerDto::class.java,
        ).also {
            assertThat(it.telefonnummerBruker).isEqualTo(telefonnummerInput.telefonnummerBruker)
        }

        kontaktRepository.findByIdOrNull(soknad.id)?.let {
            assertThat(it.telefonnummer.fraBruker).isEqualTo(telefonnummerInput.telefonnummerBruker)
        }
    }

    @Test
    fun `Utenlandsk telefonnummer skal feile`() {
        val soknad = soknadRepository.save(opprettSoknad(id = soknadId))

        val telefonnummerInput = TelefonnummerInput(telefonnummerBruker = "+4632992311")

        doPutExpectError(
            "/soknad/${soknad.id}/personalia/telefonnummer",
            telefonnummerInput,
            HttpStatus.BAD_REQUEST,
        ).also {
            assertThat(it.error).isEqualTo(SoknadApiErrorType.UgyldigInput)
        }
    }

    @Test
    fun `Telefonnummer med annet enn 8 eller 11 tegn og siffer skal feile`() {
        val soknad = soknadRepository.save(opprettSoknad(id = soknadId))

        val telefonnummerInput = TelefonnummerInput(telefonnummerBruker = "555341211")

        doPutExpectError(
            "/soknad/${soknad.id}/personalia/telefonnummer",
            telefonnummerInput,
            HttpStatus.BAD_REQUEST,
        ).also {
            assertThat(it.error).isEqualTo(SoknadApiErrorType.UgyldigInput)
        }
    }

    @Test
    fun `Telefonnummer med andre tegn og siffer skal feile`() {
        val soknad = soknadRepository.save(opprettSoknad(id = soknadId))

        val telefonnummerInput = TelefonnummerInput(telefonnummerBruker = "555-3412")

        doPutExpectError(
            "/soknad/${soknad.id}/personalia/telefonnummer",
            telefonnummerInput,
            HttpStatus.BAD_REQUEST,
        ).also {
            assertThat(it.error).isEqualTo(SoknadApiErrorType.UgyldigInput)
        }
    }

    @Test
    fun `Tomt telefonnummer skal lagres`() {
        val soknad = soknadRepository.save(opprettSoknad(id = soknadId))

        val telefonnummerInput = TelefonnummerInput()

        doPut(
            "/soknad/${soknad.id}/personalia/telefonnummer",
            telefonnummerInput,
            TelefonnummerDto::class.java,
        ).also {
            assertThat(it.telefonnummerBruker).isEqualTo(telefonnummerInput.telefonnummerBruker)
        }

        kontaktRepository.findByIdOrNull(soknad.id)?.let {
            assertThat(it.telefonnummer.fraBruker).isEqualTo(telefonnummerInput.telefonnummerBruker)
        }
    }

    // TODO Skal det være server side validering av telefonnummer?
    @Test
    fun `Oppdatere telefonnummer med annet enn siffer gir 400 BadRequest`() {
        val soknad = soknadRepository.save(opprettSoknad(id = soknadId))

        doPutExpectError(
            "/soknad/${soknad.id}/personalia/telefonnummer",
            TelefonnummerInput("asb23231"),
            HttpStatus.BAD_REQUEST,
        ).also {
            assertThat(it.error).isEqualTo(SoknadApiErrorType.UgyldigInput)
        }
    }
}
