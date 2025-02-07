package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.personalia.familie.dto.AnsvarFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.BarnFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.ForsorgerpliktFrontend
import no.nav.sosialhjelp.soknad.v2.createBarn
import no.nav.sosialhjelp.soknad.v2.createFamilie
import no.nav.sosialhjelp.soknad.v2.familie.Barn
import no.nav.sosialhjelp.soknad.v2.familie.BarnInput
import no.nav.sosialhjelp.soknad.v2.familie.Barnebidrag
import no.nav.sosialhjelp.soknad.v2.familie.EktefelleInput
import no.nav.sosialhjelp.soknad.v2.familie.Familie
import no.nav.sosialhjelp.soknad.v2.familie.FamilieRepository
import no.nav.sosialhjelp.soknad.v2.familie.ForsorgerInput
import no.nav.sosialhjelp.soknad.v2.familie.SivilstandInput
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.familie.toEktefelle
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

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
                            personId = "12345678",
                            deltBosted = true,
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
            assertThat(it.barnebidrag).isEqualTo(Barnebidrag.BETALER)
            assertThat(it.ansvar.size).isEqualTo(1)
            it.ansvar.values.firstOrNull()?.let { barn ->
                assertThat(barn.personId).isEqualTo("12345678")
                assertThat(barn.borSammen).isTrue()
            }
                ?: fail("Fant ikke barn")
        } ?: fail("Fant ikke familie")
    }

    @Test
    fun `Oppdatere deltBosted pa eksisterende barn skal lagres`() {
        val storedSoknad = soknadRepository.save(opprettSoknad())
        val personIdBarn = opprettFamilieMedBarn(storedSoknad.id)

        val barnInput =
            BarnInput(
                uuid = null,
                personId = personIdBarn,
                deltBosted = true,
            )
        val forsorgerInput = ForsorgerInput(null, listOf(barnInput))

        doPut(
            uri = "/soknad/${storedSoknad.id}/familie/forsorgerplikt",
            forsorgerInput,
            Unit::class.java,
            storedSoknad.id,
        )

        familieRepository.findByIdOrNull(storedSoknad.id)!!
            .let {
                assertThat(it.ansvar.size).isEqualTo(1)
                it.ansvar.values.firstOrNull()!!
                    .let { barn ->
                        assertThat(barn.personId).isEqualTo(personIdBarn)
                        assertThat(barn.deltBosted).isTrue()
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
        val sivilstandInput = SivilstandInput(Sivilstatus.GIFT, ektefelle)

        doPut(
            uri = "/soknad/${storedSoknad.id}/familie/sivilstatus",
            sivilstandInput,
            Unit::class.java,
            storedSoknad.id,
        )

        familieRepository.findByIdOrNull(storedSoknad.id)?.let {
            assertThat(it.sivilstatus).isEqualTo(Sivilstatus.GIFT)
            assertThat(it.ektefelle).isEqualTo(ektefelle.toEktefelle())
            assertThat(it.ektefelle?.kildeErSystem).isFalse()
        }
            ?: fail("Fant ikke familie")
    }

    private fun opprettFamilieMedBarn(soknadId: UUID): String {
        return Familie(
            soknadId = soknadId,
            harForsorgerplikt = true,
            ansvar =
                mapOf(
                    UUID.randomUUID() to
                        Barn(
                            personId = "01010112345",
                            navn = Navn(fornavn = "Ola", etternavn = "Nordmann"),
                            fodselsdato = "010101",
                        ),
                ),
        )
            .also { familieRepository.save(it) }
            .let { it.ansvar.entries.first().value.personId!! }
    }

    private fun createForsorgerpliktFrontend(personId: String): ForsorgerpliktFrontend {
        return ForsorgerpliktFrontend(
            harForsorgerplikt = true,
            barnebidrag = null,
            ansvar =
                listOf(
                    AnsvarFrontend(
                        borSammenMed = null,
                        erFolkeregistrertSammen = null,
                        harDeltBosted = true,
                        samvarsgrad = null,
                        barn =
                            BarnFrontend(
                                navn = null,
                                fodselsdato = null,
                                personnummer = personId,
                                fodselsnummer = null,
                            ),
                    ),
                ),
        )
    }
}
