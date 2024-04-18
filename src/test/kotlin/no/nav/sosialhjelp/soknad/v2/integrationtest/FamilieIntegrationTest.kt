package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.v2.createBarn
import no.nav.sosialhjelp.soknad.v2.createFamilie
import no.nav.sosialhjelp.soknad.v2.familie.BarnInput
import no.nav.sosialhjelp.soknad.v2.familie.Barnebidrag
import no.nav.sosialhjelp.soknad.v2.familie.EktefelleInput
import no.nav.sosialhjelp.soknad.v2.familie.FamilieDto
import no.nav.sosialhjelp.soknad.v2.familie.FamilieRepository
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.familie.forsorgerplikt.ForsorgerInput
import no.nav.sosialhjelp.soknad.v2.familie.sivilstatus.SivilstandInput
import no.nav.sosialhjelp.soknad.v2.familie.toDomain
import no.nav.sosialhjelp.soknad.v2.familie.toDto
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class FamilieIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var familieRepository: FamilieRepository

    @Test
    fun `Skal returnere familie`() {
        val storedSoknad = soknadRepository.save(opprettSoknad())
        val storedFamilie = familieRepository.save(createFamilie(storedSoknad.id))

        doGet(
            uri = "/soknad/${storedSoknad.id}/familie",
            responseBodyClass = FamilieDto::class.java,
        ).also {
            Assertions.assertThat(it).isEqualTo(storedFamilie.toDto())
        }
    }

    @Test
    fun `Skal oppdatere familie med forsorger`() {
        val storedSoknad = soknadRepository.save(opprettSoknad())
        familieRepository.save(
            createFamilie(
                storedSoknad.id,
                ansvar =
                    listOf(
                        createBarn(UUID.fromString("e70c6f15-0e59-4978-a6d1-cf1704594cdd"), personId = "12345678", deltBosted = true),
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

        familieRepository.findById(
            storedSoknad.id,
        ).also { Assertions.assertThat(it).isNotNull() }.get().also {
            Assertions.assertThat(it).isNotNull()
            Assertions.assertThat(it.barnebidrag).isEqualTo(Barnebidrag.BETALER)
            Assertions.assertThat(it.ansvar.size).isEqualTo(1)
            with(it.ansvar.values.first()) {
                Assertions.assertThat(personId).isEqualTo("12345678")
                Assertions.assertThat(deltBosted).isEqualTo(true)
            }
        }
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

        familieRepository.findById(
            storedSoknad.id,
        ).also { Assertions.assertThat(it).isNotNull() }.get().also {
            Assertions.assertThat(it).isNotNull()
            Assertions.assertThat(it.sivilstatus).isEqualTo(Sivilstatus.GIFT)
            Assertions.assertThat(it.ektefelle).isEqualTo(ektefelle.toDomain())
            Assertions.assertThat(it.ektefelle?.kildeErSystem).isFalse()
        }
    }
}
