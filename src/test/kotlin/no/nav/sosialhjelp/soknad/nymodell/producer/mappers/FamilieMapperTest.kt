package no.nav.sosialhjelp.soknad.nymodell.producer.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.nymodell.domene.Kilde
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.Barn
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.Ektefelle
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.Forsorger
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.Sivilstand
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.ForsorgerRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.SivilstandRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.Barnebidrag
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.nymodell.domene.Navn
import no.nav.sosialhjelp.soknad.nymodell.producer.json.createChildrenIfNotExists
import no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.FamilieMapper
import no.nav.sosialhjelp.soknad.nymodell.repository.RepositoryTest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import java.util.*

@Import(FamilieMapper::class)
class FamilieMapperTest: RepositoryTest() {

    @Autowired
    private lateinit var familieMapper: FamilieMapper
    @Autowired
    private lateinit var sivilstandRepository: SivilstandRepository
    @Autowired
    private lateinit var forsorgerRepository: ForsorgerRepository

    @Test
    fun `Map familie til JsonInternalSoknad`() {
        val nySoknad = opprettSoknad()

        val sivilstand: Sivilstand = createSivilstand(nySoknad.id)
        val forsorger: Forsorger = createForsorgerMedBarn(nySoknad.id)

        val json = JsonInternalSoknad().apply { createChildrenIfNotExists() }
        familieMapper.mapDomainToJson(nySoknad.id, json)

        with (json) {
            soknad.data.familie.sivilstatus.let {
                assertThat(it.borSammenMed).isEqualTo(sivilstand.ektefelle?.borSammen)
                assertThat(it.ektefelle.navn.etternavn).isEqualTo(sivilstand.ektefelle?.navn?.etternavn)
                assertThat(it.status.name).isEqualTo(sivilstand.sivilstatus?.name)
            }

            soknad.data.familie.forsorgerplikt.let {
                assertThat(it.barnebidrag.verdi.name).isEqualTo(forsorger.barnebidrag?.name)
                assertThat(it.harForsorgerplikt.verdi).isEqualTo(forsorger.harForsorgerplikt)

                it.ansvar.forEach { ansvar ->
                    assertThat(
                        forsorger.barn.find { domainBarn ->
                            domainBarn.navn?.etternavn == ansvar.barn.navn.etternavn
                        }).isNotNull
                }
            }
        }
    }

    private fun createForsorgerMedBarn(soknadId: UUID): Forsorger {
        return Forsorger(
            soknadId,
            harForsorgerplikt = true,
            barnebidrag = Barnebidrag.INGEN,
            barn = setOf(
                Barn(
                    personId = "123454321",
                    navn = Navn("Barn", "Barnie", "Barnesen"),
                    borSammen = true,
                    folkeregistrertMed = true
                )
            )
        ).also { forsorgerRepository.save(it) }
    }

    private fun createSivilstand(soknadId: UUID): Sivilstand {
        return Sivilstand(
            soknadId,
            kilde = Kilde.BRUKER,
            sivilstatus = Sivilstatus.GIFT,
            ektefelle = Ektefelle(
                personId = "12345",
                navn = Navn("Kvinne", "Kvinn", "Kvinnesen"),
                borSammen = true
            )
        ).also { sivilstandRepository.save(it) }
    }

}