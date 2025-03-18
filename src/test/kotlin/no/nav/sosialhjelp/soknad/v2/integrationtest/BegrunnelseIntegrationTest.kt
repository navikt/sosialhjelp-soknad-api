package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.BegrunnelseDto
import no.nav.sosialhjelp.soknad.v2.soknad.HarHvaSokesOmInput
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull

class BegrunnelseIntegrationTest : AbstractIntegrationTest() {
    @Test
    fun `Hente begrunnelse skal returnere riktig data`() {
        val soknad = soknadRepository.save(opprettSoknad(id = soknadId))

        doGet(
            "/soknad/${soknad.id}/begrunnelse",
            BegrunnelseDto::class.java,
        ).also {
            assertThat(it.hvorforSoke).isEqualTo(soknad.begrunnelse.hvorforSoke)
            assertThat(it.hvaSokesOm).isEqualTo(soknad.begrunnelse.hvaSokesOm)
        }
    }

    @Test
    fun `Oppdatere begrunnelse skal lagres i databasen`() {
        val soknad = soknadRepository.save(opprettSoknad(id = soknadId))

        val input =
            HarHvaSokesOmInput(
                hvaSokesOm = "Jeg bare må ha penger",
                hvorforSoke = "Fordi jeg ikke har penger vel",
            )

        doPut(
            "/soknad/${soknad.id}/begrunnelse",
            input,
            BegrunnelseDto::class.java,
            soknad.id,
        )

        soknadRepository.findByIdOrNull(soknad.id)?.let {
            assertThat(it.begrunnelse.hvaSokesOm).isEqualTo(input.hvaSokesOm)
            assertThat(it.begrunnelse.hvorforSoke).isEqualTo(input.hvorforSoke)
        }
            ?: fail("Feil i test")
    }

    @Test
    fun `Input hvor ett felt er tomt skal lagres i databasen`() {
        val soknad = soknadRepository.save(opprettSoknad(id = soknadId))

        val input =
            HarHvaSokesOmInput(
                hvaSokesOm = "",
                hvorforSoke = "Fordi jeg ikke har penger vel",
            )

        doPut(
            "/soknad/${soknad.id}/begrunnelse",
            input,
            BegrunnelseDto::class.java,
            soknad.id,
        )

        soknadRepository.findByIdOrNull(soknad.id)?.let {
            assertThat(it.begrunnelse.hvaSokesOm).isEqualTo("")
            assertThat(it.begrunnelse.hvorforSoke).isEqualTo(input.hvorforSoke)
        }
            ?: fail("Feil i test")
    }
}
