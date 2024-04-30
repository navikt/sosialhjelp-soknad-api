package no.nav.sosialhjelp.soknad.v2.integrationtest

import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.createBarn
import no.nav.sosialhjelp.soknad.v2.createFamilie
import no.nav.sosialhjelp.soknad.v2.familie.BarnInput
import no.nav.sosialhjelp.soknad.v2.familie.Barnebidrag
import no.nav.sosialhjelp.soknad.v2.familie.EktefelleInput
import no.nav.sosialhjelp.soknad.v2.familie.FamilieRepository
import no.nav.sosialhjelp.soknad.v2.familie.ForsorgerInput
import no.nav.sosialhjelp.soknad.v2.familie.SivilstandInput
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.familie.toDomain
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class FamilieIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var familieRepository: FamilieRepository

    @Test
    fun `Skal oppdatere familie med forsorger`() {
        val storedSoknad = soknadRepository.save(opprettSoknad())
        familieRepository.save(
            createFamilie(
                storedSoknad.id,
                ansvar =
                listOf(
                    createBarn(
                        UUID.fromString("e70c6f15-0e59-4978-a6d1-cf1704594cdd"),
                        personId = "12345678",
                        deltBosted = true
                    ),
                ),
            ),
        )

        val barnInput =
            BarnInput(
                uuid = UUID.fromString("e70c6f15-0e59-4978-a6d1-cf1704594cdd"),
                deltBosted = true,
            )
        val forsorgerInput = ForsorgerInput(Barnebidrag.BETALER, listOf(barnInput))

        doPut(
            uri = "/soknad/${storedSoknad.id}/familie/forsorgerplikt",
            forsorgerInput,
            Unit::class.java,
            storedSoknad.id,
        )

        familieRepository.findByIdOrNull(storedSoknad.id)?.let {
            assertThat(it.forsorger.barnebidrag).isEqualTo(Barnebidrag.BETALER)
            assertThat(it.forsorger.ansvar.size).isEqualTo(1)
            it.forsorger.ansvar.values.firstOrNull()?.let { barn ->
                assertThat(barn.personId).isEqualTo("12345678")
                assertThat(barn.borSammen).isTrue()
            }
                ?: fail("Fant ikke barn")
        } ?: fail("Fant ikke familie")
    }

    @Test
    fun `Skal oppdatere familie med ektefelle`() {
        val storedSoknad = soknadRepository.save(opprettSoknad())
        familieRepository.save(createFamilie(storedSoknad.id, ektefelle = null))

        val ektefelle =
            EktefelleInput(
                personId = "12345678",
                navn = Navn(fornavn = "Mr.", etternavn = "Cool"),
                fodselsdato = "10101900",
                borSammen = true,
            )
        val forsorgerInput = SivilstandInput(Sivilstatus.GIFT, ektefelle)

        doPut(
            uri = "/soknad/${storedSoknad.id}/familie/sivilstatus",
            forsorgerInput,
            Unit::class.java,
            storedSoknad.id,
        )

        familieRepository.findByIdOrNull(storedSoknad.id)?.let {
            assertThat(it.sivilstand.sivilstatus).isEqualTo(Sivilstatus.GIFT)
            assertThat(it.sivilstand.ektefelle).isEqualTo(ektefelle.toDomain())
            assertThat(it.sivilstand.ektefelle?.kildeErSystem).isFalse()
        }
            ?: fail("Fant ikke familie")
    }
}
