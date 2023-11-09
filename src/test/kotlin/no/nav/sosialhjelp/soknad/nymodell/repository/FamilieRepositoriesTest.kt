//package no.nav.sosialhjelp.soknad.nymodell.repository
//
//import no.nav.sosialhjelp.soknad.nymodell.domene.Kilde
//import no.nav.sosialhjelp.soknad.nymodell.domene.familie.Barn
//import no.nav.sosialhjelp.soknad.nymodell.domene.familie.Ektefelle
//import no.nav.sosialhjelp.soknad.nymodell.domene.familie.Forsorger
//import no.nav.sosialhjelp.soknad.nymodell.domene.familie.Sivilstand
//import no.nav.sosialhjelp.soknad.nymodell.domene.familie.repository.ForsorgerRepository
//import no.nav.sosialhjelp.soknad.nymodell.domene.familie.repository.SivilstandRepository
//import no.nav.sosialhjelp.soknad.nymodell.domene.familie.type.Sivilstatus
//import org.assertj.core.api.Assertions.assertThat
//import org.assertj.core.api.Assertions.assertThatThrownBy
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.dao.DuplicateKeyException
//
//class FamilieRepositoriesTest: RepositoryTest() {
//
//    @Autowired
//    private lateinit var sivilstandRepository: SivilstandRepository
//    @Autowired
//    private lateinit var forsorgerRepository: ForsorgerRepository
//
//    @Test
//    fun `Lagre ny Sivilstand`() {
//        val soknad = opprettSoknad()
//
//        Sivilstand(
//            soknadId = soknad.soknadId,
//            kilde = Kilde.BRUKER,
//            sivilstatus = Sivilstatus.GIFT,
//            ektefelle = Ektefelle(
//                personId = "12315125125",
//                fornavn = "Kari",
//                etternavn = "Nordmann"
//            )
//        ).also {
//            sivilstandRepository.save(it)
//        }
//
//        sivilstandRepository.findById(soknad.soknadId).get().let {
//            assertThat(it.sivilstatus).isEqualTo(Sivilstatus.GIFT)
//        } ?: throw IllegalStateException("Sivilstand ble ikke lagret")
//    }
//
////    @Test
////    fun `Lagre ny Forsorger med 2 barn`() {
////        val forsorger = Forsorger(
////            soknadId = opprettSoknad().soknadId,
////            harForsorgerplikt = true,
////            barn = setOf(
////                Barn(
////                    personId = "12314151231",
////                    fornavn = "Barn",
////                    etternavn = "Barnesen",
////                ),
////                Barn(
////                    personId = "5342344324",
////                    fornavn = "Jent",
////                    etternavn = "Jentesen",
////                )
////            )
////        ).also { forsorgerRepository.save(it) }
////
////        forsorgerRepository.findById(forsorger.soknadId).get().let {
////            assertThat(it.barn.size).isEqualTo(2)
////            assertThat(it.harForsorgerplikt).isTrue()
////        } ?: throw IllegalStateException("Forsorger ble ikke lagret")
////    }
//
//    @Test
//    fun `Kan ikke lagre samme Barn pa samme soknad`() {
//        val soknad = opprettSoknad()
//
//        val setOfBarn = setOf(
//            Barn(personId = "12345612345", borSammen = false),
//            Barn(personId = "12345612345", borSammen = true)
//        )
//
//        assertThatThrownBy {
//            forsorgerRepository.save(Forsorger(soknad.soknadId, barn = setOfBarn))
//        }.cause().isInstanceOf(DuplicateKeyException::class.java)
//    }
//
//    @Test
//    fun `Tabeller tommes ved sletting av soknad`() {
//        val soknad = opprettSoknad()
//
//        Forsorger(soknad.soknadId, harForsorgerplikt = true, barn = setOf(
//            Barn(personId = "12345612345", borSammen = false),
//            Barn(personId = "12345612346", borSammen = true)
//        )).also { forsorgerRepository.save(it) }
//
//        Ektefelle(personId = "12345123451").also { sivilstandRepository.save(
//            Sivilstand(
//                soknadId = soknad.soknadId,
//                kilde = Kilde.BRUKER,
//                sivilstatus = Sivilstatus.GIFT,
//                ektefelle = it
//            )
//        ) }
//
//        assertThat(getAntallBarn()).isEqualTo(2)
//        assertThat(ektefelleExists()).isEqualTo(1)
//
//        soknadRepository.delete(soknad)
//
//        assertThat(getAntallBarn()).isEqualTo(0)
//        assertThat(ektefelleExists()).isEqualTo(0)
//    }
//
//    private fun getAntallBarn(): Int = jdbcTemplate.queryForObject(
//        "SELECT count(*) FROM barn", Int::class.java
//    ) as Int
//
//    private fun ektefelleExists(): Int = jdbcTemplate.queryForObject(
//        "SELECT count(*) FROM ektefelle", Int::class.java
//    ) as Int
//
//}
