package no.nav.sosialhjelp.soknad.repository

import no.nav.sosialhjelp.soknad.model.familie.Barn
import no.nav.sosialhjelp.soknad.model.familie.Barnebidrag
import no.nav.sosialhjelp.soknad.model.familie.Ektefelle
import no.nav.sosialhjelp.soknad.model.familie.Familie
import no.nav.sosialhjelp.soknad.model.familie.Sivilstatus
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import java.util.*

class FamilieRepositoryTest: RepositoryTest() {

    @Autowired
    private lateinit var familieRepository: FamilieRepository

    @Test
    fun `Lagre nytt relasjoner-objekt`() {

        val soknad = opprettSoknad()

        val relasjoner = opprettFamilieObjekt(soknad.id)
        familieRepository.save(relasjoner)

        val lagretRelasjoner = familieRepository.findById(relasjoner.id).get()
        assertThat(lagretRelasjoner).isEqualTo(relasjoner)
    }

    @Test
    fun `Kan ikke lagre samme Barn pa samme soknad`() {
        val soknad = opprettSoknad()

        val setOfBarn = setOf(
            Barn(soknad.id, "12345612345", borSammen = false),
            Barn(soknad.id, "12345612345", borSammen = true)
        )

        assertThatThrownBy {
            familieRepository.save(Familie(soknad.id, ansvar = setOfBarn))
        }.cause().isInstanceOf(DuplicateKeyException::class.java)
    }

    @Test
    fun `Tabeller tommes ved sletting av soknad`() {
        val soknad = opprettSoknad()
        val familie = opprettFamilieObjekt(soknadId = soknad.id)
        familieRepository.save(familie)

        assertThat(getAntallBarn()).isEqualTo(2)
        assertThat(ektefelleExists()).isEqualTo(1)

        soknadRepository.delete(soknad)

        assertThat(getAntallBarn()).isEqualTo(0)
        assertThat(ektefelleExists()).isEqualTo(0)
    }

    private fun getAntallBarn(): Int = jdbcTemplate.queryForObject(
        "SELECT count(*) FROM barn", Int::class.java
    ) as Int

    private fun ektefelleExists(): Int = jdbcTemplate.queryForObject(
        "SELECT count(*) FROM ektefelle", Int::class.java
    ) as Int

    private fun opprettFamilieObjekt(soknadId: UUID): Familie {

        return Familie (
            soknadId = soknadId,
            harForsorgerplikt = true,
            barnebidrag = Barnebidrag.INGEN,
            ansvar = opprettAnsvar(soknadId),
            sivilstatus = Sivilstatus.GIFT,
            ektefelle = opprettEktefelle(soknadId)
        )
    }

    private fun opprettAnsvar(soknadId: UUID): Set<Barn> {
        return setOf(
            Barn(soknadId, "123443211232"),
            Barn(soknadId, "123123123123")
        )
    }

    private fun opprettEktefelle(soknadId: UUID): Ektefelle {
        return Ektefelle(
            soknadId,
            "12345612345"
        )
    }
}
