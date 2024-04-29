package no.nav.sosialhjelp.soknad.v2.register.handlers.person

import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import no.nav.sosialhjelp.soknad.v2.eier.Kontonummer
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.kontoDto
import no.nav.sosialhjelp.soknad.v2.register.handlers.AbstractHandlePersonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class HandleEierDataTest: AbstractHandlePersonTest() {

    @Autowired
    private lateinit var eierRepository: EierRepository

    @Test
    fun `Hente fra PDL skal lagre eier-data i db`() {
        val personDto = createAnswerForHentPersonUgift()

        handlePerson.handle(soknad.id)

        eierRepository.findByIdOrNull(soknad.id)?.let {
            assertThat(it.navn.fornavn).isEqualTo(personDto.navn?.get(0)?.fornavn)
            assertThat(it.navn.etternavn).isEqualTo(personDto.navn?.get(0)?.etternavn)
            assertThat(it.statsborgerskap).isEqualTo(personDto.statsborgerskap?.get(0)?.land)
            assertThat(it.kontonummer.fraRegister).isEqualTo(kontoDto.kontonummer)
        }
            ?: fail("Fant ikke eier")
    }

    @Test
    fun `Eier-data skal overskrives ved ny innhenting, men kontonummer fra bruker skal bestå`() {
        val existing = eierRepository.save(
            Eier(
                soknadId = soknad.id,
                statsborgerskap = "NOR",
                nordiskBorger = true,
                navn = Navn(
                    fornavn = "Fornavn",
                    etternavn = "Etternavnesen"
                ),
                kontonummer = Kontonummer(fraBruker = "98769898765")
            )
        )
        val personDto = createAnswerForHentPersonUgift()
        handlePerson.handle(soknad.id)

        eierRepository.findByIdOrNull(soknad.id)?.let { updated ->
            assertThat(existing.navn).isNotEqualTo(updated.navn)
            assertThat(updated.navn.fornavn).isEqualTo(personDto.navn?.get(0)?.fornavn)

            assertThat(existing.kontonummer.fraRegister).isNull()
            assertThat(updated.kontonummer.fraRegister).isEqualTo(kontoDto.kontonummer)
            assertThat(updated.kontonummer.fraBruker).isEqualTo(existing.kontonummer.fraBruker)
        }
            ?: fail("Fant ikke Eier")
    }
}
