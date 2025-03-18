package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.BegrunnelseDto
import no.nav.sosialhjelp.soknad.v2.soknad.HarHvaSokesOmInput
import no.nav.sosialhjelp.soknad.v2.soknad.HarKategorierInput
import no.nav.sosialhjelp.soknad.v2.soknad.Kategori
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

    @Test
    fun `Oppdatere Kategorier skal lagres i databasen`() {
        val soknad = soknadRepository.save(opprettSoknad())

        val definerteKategorier = setOf(Kategori.Husleie, Kategori.Livsopphold, Kategori.Nodhjelp.IkkeMat)
        val annetKategorier = "Trenger også penger til bil"

        val input =
            HarKategorierInput(
                hvorforSoke = "Fordi jeg ikke har penger vel",
                kategorier = definerteKategorier,
                annet = annetKategorier,
            )

        doPut(
            "/soknad/${soknad.id}/begrunnelse",
            input,
            BegrunnelseDto::class.java,
            soknad.id,
        )
            .also { dto ->
                assertThat(dto.kategorier.definerte.toList())
                    .hasSize(3)
                    .allMatch { definerteKategorier.contains(it) }

                assertThat(dto.kategorier.annet).isEqualTo(annetKategorier)
            }

        soknadRepository.findByIdOrNull(soknad.id)!!.begrunnelse.kategorier
            .also { begrunnelse ->
                assertThat(begrunnelse.definerte.toList())
                    .hasSize(3)
                    .anyMatch { it is Kategori.Husleie }
                    .anyMatch { it is Kategori.Livsopphold }
                    .anyMatch { it is Kategori.Nodhjelp.IkkeMat }
            }
    }
}
