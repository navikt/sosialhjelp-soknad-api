package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import no.nav.sosialhjelp.soknad.v2.opprettEier
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.HarIkkeKontoInput
import no.nav.sosialhjelp.soknad.v2.soknad.KontoInformasjonDto
import no.nav.sosialhjelp.soknad.v2.soknad.KontonummerBrukerInput
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class KontonummerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var eierRepository: EierRepository

    @Test
    fun `Hente kontonummer skal returnere lagrede data`() {
        val soknad = soknadRepository.save(opprettSoknad())
        val eier = eierRepository.save(opprettEier(soknad.id, soknad.eierPersonId))

        doGet(
            "/soknad/${soknad.id}/personalia/kontonummer",
            KontoInformasjonDto::class.java,
        ).also {
            assertThat(it.kontonummerBruker).isEqualTo(eier.kontonummer!!.fraBruker)
            assertThat(it.kontonummerRegister).isEqualTo(eier.kontonummer!!.fraRegister)
            assertThat(it.harIkkeKonto).isEqualTo(eier.kontonummer!!.harIkkeKonto)
        }
    }

    @Test
    fun `Oppdatere brukers kontonummer skal lagres i db`() {
        val soknadId = createSoknadOgEier()

        val input = KontonummerBrukerInput(kontonummer = "12345312345")
        doPut(
            "/soknad/$soknadId/personalia/kontonummer",
            input,
            KontoInformasjonDto::class.java,
            soknadId,
        )

        eierRepository.findByIdOrNull(soknadId)?.let {
            assertThat(it.kontonummer!!.harIkkeKonto).isNull()
            assertThat(it.kontonummer!!.fraBruker).isEqualTo(input.kontonummer)
        }
            ?: fail("Fant ikke brukerdata")
    }

    @Test
    fun `Sette HarIkkeKonto skal lagres i basen, og kontonummerBruker == null`() {
        val soknadId = createSoknadOgEier()

        doPut(
            "/soknad/$soknadId/personalia/kontonummer",
            HarIkkeKontoInput(true),
            KontoInformasjonDto::class.java,
            soknadId,
        )

        eierRepository.findByIdOrNull(soknadId)?.let {
            assertThat(it.kontonummer!!.harIkkeKonto).isTrue()
            assertThat(it.kontonummer!!.fraBruker).isNull()
        }
            ?: fail("Fant ikke brukerdata")
    }

    private fun createSoknadOgEier(): UUID {
        return soknadRepository.save(opprettSoknad(UUID.randomUUID()))
            .also { eierRepository.save(opprettEier(it.id, it.eierPersonId)) }
            .id
    }
}
