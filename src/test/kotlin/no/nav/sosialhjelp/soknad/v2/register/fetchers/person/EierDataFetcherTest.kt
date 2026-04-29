package no.nav.sosialhjelp.soknad.v2.register.fetchers.person

import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import no.nav.sosialhjelp.soknad.v2.eier.Kontonummer
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.register.fetchers.AbstractPersonDataFetcherTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class EierDataFetcherTest : AbstractPersonDataFetcherTest() {
    @Autowired
    private lateinit var eierRepository: EierRepository

    @Test
    fun `Hente fra PDL skal lagre eier-data i db`() {
        val personDto = createAnswerForHentPersonUgift()

        fetchPerson.fetchAndSave(soknad.id)

        eierRepository.findByIdOrNull(soknad.id)?.let {
            assertThat(it.navn.fornavn).isEqualTo(personDto.navn?.get(0)?.fornavn)
            assertThat(it.navn.etternavn).isEqualTo(personDto.navn?.get(0)?.etternavn)
            assertThat(it.statsborgerskap).isEqualTo(personDto.statsborgerskap?.get(0)?.land)
        }
            ?: fail("Fant ikke eier")
    }

    @Test
    fun `Eier-data skal overskrives ved ny innhenting, men kontonummer skal bestå`() {
        val existing =
            eierRepository.save(
                Eier(
                    soknadId = soknad.id,
                    statsborgerskap = "NOR",
                    nordiskBorger = true,
                    navn =
                        Navn(
                            fornavn = "Fornavn",
                            etternavn = "Etternavnesen",
                        ),
                    kontonummer = Kontonummer(fraBruker = "98769898765", fraRegister = "12345678901"),
                ),
            )
        val personDto = createAnswerForHentPersonUgift()
        fetchPerson.fetchAndSave(soknad.id)

        eierRepository.findByIdOrNull(soknad.id)!!.let { updated ->
            assertThat(existing.navn).isNotEqualTo(updated.navn)
            assertThat(updated.navn.fornavn).isEqualTo(personDto.navn?.get(0)?.fornavn)

            assertThat(updated.kontonummer.fraRegister).isEqualTo(existing.kontonummer.fraRegister)
            assertThat(updated.kontonummer.fraBruker).isEqualTo(existing.kontonummer.fraBruker)
        }
    }
}
