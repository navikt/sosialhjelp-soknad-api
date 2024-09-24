package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.livssituasjon.BosituasjonDto
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Botype
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRepository
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import no.nav.sosialhjelp.soknad.v2.opprettLivssituasjon
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class BosituasjonIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var livssituasjonRepository: LivssituasjonRepository

    @Autowired
    private lateinit var dokumentasjonRepository: DokumentasjonRepository

    @Test
    fun `Hente Bosituasjon skal returnere korrekte data`() {
        val soknad = soknadRepository.save(opprettSoknad())
        val livssituasjon = livssituasjonRepository.save(opprettLivssituasjon(soknad.id))

        doGet(
            "/soknad/${soknad.id}/bosituasjon",
            BosituasjonDto::class.java,
        ).also {
            assertThat(it.botype).isEqualTo(livssituasjon.bosituasjon!!.botype)
            assertThat(it.antallPersoner).isEqualTo(livssituasjon.bosituasjon!!.antallHusstand)
        }
    }

    @Test
    fun `Oppdatere Bosituasjon skal lagres i databasen`() {
        val soknad = soknadRepository.save(opprettSoknad())

        val bosituasjonInput =
            BosituasjonDto(
                botype = Botype.EIER,
                antallPersoner = 3,
            )

        doPut(
            "/soknad/${soknad.id}/bosituasjon",
            bosituasjonInput,
            BosituasjonDto::class.java,
            soknad.id,
        )

        livssituasjonRepository.findByIdOrNull(soknad.id)?.let {
            assertThat(it.bosituasjon!!.botype).isEqualTo(bosituasjonInput.botype)
            assertThat(it.bosituasjon!!.antallHusstand).isEqualTo(bosituasjonInput.antallPersoner)
        }
            ?: fail("Fant ikke brukerdata")
    }

    @Test
    fun `Dto med en null-verdi skal lagres i databasen`() {
        val soknad = soknadRepository.save(opprettSoknad())

        doPut(
            "/soknad/${soknad.id}/bosituasjon",
            BosituasjonDto(antallPersoner = 3),
            BosituasjonDto::class.java,
            soknad.id,
        )

        livssituasjonRepository.findByIdOrNull(soknad.id)?.let {
            assertThat(it.bosituasjon!!.botype).isNull()
            assertThat(it.bosituasjon!!.antallHusstand).isEqualTo(3)
        }
            ?: fail("Kunne ikke finne brukerdata")
    }

    @Test
    fun `Botype LEIER skal opprette Dokumentasjon`() {
        val soknad = soknadRepository.save(opprettSoknad())

        doPut(
            "/soknad/${soknad.id}/bosituasjon",
            BosituasjonDto(botype = Botype.LEIER),
            BosituasjonDto::class.java,
            soknad.id,
        )

        dokumentasjonRepository.findAllBySoknadId(soknad.id).let {
            assertThat(it).anyMatch { it.type == UtgiftType.UTGIFTER_HUSLEIE }
        }
    }

    @Test
    fun `Botype KOMMUNAL skal opprette Dokumentasjon`() {
        val soknad = soknadRepository.save(opprettSoknad())

        doPut(
            "/soknad/${soknad.id}/bosituasjon",
            BosituasjonDto(botype = Botype.KOMMUNAL),
            BosituasjonDto::class.java,
            soknad.id,
        )

        dokumentasjonRepository.findAllBySoknadId(soknad.id).let {
            assertThat(it).anyMatch { it.type == UtgiftType.UTGIFTER_HUSLEIE_KOMMUNAL }
        }
    }
}
