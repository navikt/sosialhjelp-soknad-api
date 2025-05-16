package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.opprettEier
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.KontoinformasjonDto
import no.nav.sosialhjelp.soknad.v2.soknad.KontoinformasjonInput
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID
import kotlin.jvm.java

class KontonummerIntegrationTest : AbstractIntegrationTest() {
    @Test
    fun `Hente kontonummer skal returnere lagrede data`() {
        val soknad = soknadRepository.save(opprettSoknad(id = soknadId))
        val eier = eierRepository.save(opprettEier(soknad.id, soknad.eierPersonId))

        doGet(
            "/soknad/${soknad.id}/personalia/kontonummer",
            KontoinformasjonDto::class.java,
        ).also {
            assertThat(it.kontonummerBruker).isEqualTo(eier.kontonummer.fraBruker)
            assertThat(it.kontonummerRegister).isEqualTo(eier.kontonummer.fraRegister)
            assertThat(it.harIkkeKonto).isEqualTo(eier.kontonummer.harIkkeKonto)
        }
    }

    @Test
    fun `Oppdatere brukers kontonummer skal lagres i db`() {
        val soknadId = createSoknadOgEier()

        val input = KontoinformasjonInput(kontonummerBruker = "12345312345")
        doPut(
            "/soknad/$soknadId/personalia/kontonummer",
            input,
            KontoinformasjonDto::class.java,
            soknadId,
        )

        eierRepository.findByIdOrNull(soknadId)?.let {
            assertThat(it.kontonummer.harIkkeKonto).isNull()
            assertThat(it.kontonummer.fraBruker).isEqualTo(input.kontonummerBruker)
        }
            ?: fail("Fant ikke brukerdata")
    }

    @Test
    fun `Sette HarIkkeKonto skal lagres i basen, og kontonummerBruker == null`() {
        val soknadId = createSoknadOgEier()

        doPut(
            "/soknad/$soknadId/personalia/kontonummer",
            KontoinformasjonInput(harIkkeKonto = true),
            KontoinformasjonDto::class.java,
            soknadId,
        )

        eierRepository.findByIdOrNull(soknadId)?.let {
            assertThat(it.kontonummer.harIkkeKonto).isTrue()
            assertThat(it.kontonummer.fraBruker).isNull()
        }
            ?: fail("Fant ikke brukerdata")
    }

    private fun createSoknadOgEier(): UUID {
        return soknadRepository.save(opprettSoknad(soknadId))
            .also { eierRepository.save(opprettEier(it.id, it.eierPersonId)) }
            .id
    }
}
