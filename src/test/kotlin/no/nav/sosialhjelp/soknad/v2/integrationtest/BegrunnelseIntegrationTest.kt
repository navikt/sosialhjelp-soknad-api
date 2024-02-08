package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataPersonRepository
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.BegrunnelseDto
import no.nav.sosialhjelp.soknad.v2.createSoknad
import no.nav.sosialhjelp.soknad.v2.opprettBrukerdataPerson
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus

class BegrunnelseIntegrationTest: AbstractIntegrationTest() {

    @Autowired
    private lateinit var brukerdataPersonRepository: BrukerdataPersonRepository

    @Test
    fun `Hente begrunnelse skal returnere riktig data`() {
        val soknad = soknadRepository.save(createSoknad())
        val brukerdata = brukerdataPersonRepository.save(opprettBrukerdataPerson(soknad.id!!))

        doGet(
            "/soknad/${soknad.id}/begrunnelse",
            BegrunnelseDto::class.java
        ).also {
            assertThat(it.hvorforSoke).isEqualTo(brukerdata.begrunnelse?.hvorforSoke)
            assertThat(it.hvaSokesOm).isEqualTo(brukerdata.begrunnelse?.hvaSokesOm)
        }
    }

    @Test
    fun `Oppdatere begrunnelse skal lagres i databasen`() {
        val soknad = soknadRepository.save(createSoknad())

        val inputBegrunnelse = BegrunnelseDto(
            hvaSokesOm = "Jeg bare må ha penger",
            hvorforSoke = "Fordi jeg ikke har penger vel"
        )

        doPut(
            "/soknad/${soknad.id}/begrunnelse",
            inputBegrunnelse,
            BegrunnelseDto::class.java
        )

        brukerdataPersonRepository.findByIdOrNull(soknad.id!!)?.let {
            assertThat(it.begrunnelse?.hvaSokesOm).isEqualTo(inputBegrunnelse.hvaSokesOm)
            assertThat(it.begrunnelse?.hvorforSoke).isEqualTo(inputBegrunnelse.hvorforSoke)
        }
            ?: fail("Feil i test")
    }

    @Test
    fun `Oppdatere begrunnelse med ulovlige tegn skal gi 400 BadRequest`() {
        val soknad = soknadRepository.save(createSoknad())

        doPutExpectError(
            "/soknad/${soknad.id}/begrunnelse",
            BegrunnelseDto(
                hvorforSoke = "Jeg bare trenger penger nå %¤/%¤&%¤&",
                hvaSokesOm = "Masse penger"
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    @Test
    fun `Oppdatere begrunnelse med tomt innhold skal gi 400 BadRequest`() {
        val soknad = soknadRepository.save(createSoknad())

        doPutExpectError(
            "/soknad/${soknad.id}/begrunnelse",
            BegrunnelseDto(
                hvaSokesOm = null,
                hvorforSoke = null
            ),
            HttpStatus.BAD_REQUEST
        )

        doPutExpectError(
            "/soknad/${soknad.id}/begrunnelse",
            BegrunnelseDto(
                hvaSokesOm = "",
                hvorforSoke = ""
            ),
            HttpStatus.BAD_REQUEST
        )

        doPutExpectError(
            "/soknad/${soknad.id}/begrunnelse",
            BegrunnelseDto(
                hvaSokesOm = "   ",
                hvorforSoke = "    "
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    @Test
    fun `Input hvor ett felt er tomt skal lagres i databasen`() {
        val soknad = soknadRepository.save(createSoknad())

        val inputBegrunnelse = BegrunnelseDto(
            hvaSokesOm = null,
            hvorforSoke = "Fordi jeg ikke har penger vel"
        )

        doPut(
            "/soknad/${soknad.id}/begrunnelse",
            inputBegrunnelse,
            BegrunnelseDto::class.java
        )

        brukerdataPersonRepository.findByIdOrNull(soknad.id!!)?.let {
            assertThat(it.begrunnelse?.hvaSokesOm).isNull()
            assertThat(it.begrunnelse?.hvorforSoke).isEqualTo(inputBegrunnelse.hvorforSoke)
        }
            ?: fail("Feil i test")
    }
}
