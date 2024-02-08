package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataPerson
import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataPersonRepository
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.KontoInformasjonDto
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.KontoInformasjonInput
import no.nav.sosialhjelp.soknad.v2.createEier
import no.nav.sosialhjelp.soknad.v2.createSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus

class KontonummerIntegrationTest: AbstractIntegrationTest() {

    @Autowired
    private lateinit var brukerdataPersonRepository: BrukerdataPersonRepository

    @Test
    fun `Hente kontonummer skal returnere lagrede data`() {
        val soknad = soknadRepository.save(createSoknad())
        val brukerdataPerson = brukerdataPersonRepository.save(BrukerdataPerson(soknad.id!!))

        doGet(
            "/soknad/${soknad.id}/personalia/kontonummer",
            KontoInformasjonDto::class.java
        ).also {
            assertThat(it.kontonummerBruker).isEqualTo(brukerdataPerson.kontoInformasjon?.kontonummer)
            assertThat(it.kontonummerRegister).isEqualTo(soknad.eier.kontonummer)
            assertThat(it.harIkkeKonto).isEqualTo(brukerdataPerson.kontoInformasjon?.harIkkeKonto)
        }
    }

    @Test
    fun `Oppdatere kontoinfo skal lagres i databasen`() {
        val soknad = soknadRepository.save(createSoknad())

        val kontoInformasjonInput = KontoInformasjonInput(
            harIkkeKonto = false,
            kontonummerBruker = "12341212345"
        )

        doPut(
            "/soknad/${soknad.id}/personalia/kontonummer",
            kontoInformasjonInput,
            KontoInformasjonDto::class.java
        )

        brukerdataPersonRepository.findByIdOrNull(soknad.id)?.let {
            assertThat(it.kontoInformasjon?.harIkkeKonto).isEqualTo(kontoInformasjonInput.harIkkeKonto)
            assertThat(it.kontoInformasjon?.kontonummer).isEqualTo(kontoInformasjonInput.kontonummerBruker)
        }
            ?: fail("Fant ikke brukerdata")
    }

    @Test
    fun `HarIkkeKonto = true skal gi feil hvis det finnes et kontonummer`() {
        val soknad = soknadRepository.save(createSoknad())

        doPutExpectError(
            "/soknad/${soknad.id}/personalia/kontonummer",
            KontoInformasjonInput(true, null),
            HttpStatus.BAD_REQUEST
        )
    }

    @Test
    fun `HarIkkeKonto = false skal gi feil hvis det ikke finnes et kontonummer`() {
        val soknad = soknadRepository.save(createSoknad(eier = createEier(kontonummer = null)))

        doPutExpectError(
            "/soknad/${soknad.id}/personalia/kontonummer",
            KontoInformasjonInput(false, null),
            HttpStatus.BAD_REQUEST
        )
    }
}
