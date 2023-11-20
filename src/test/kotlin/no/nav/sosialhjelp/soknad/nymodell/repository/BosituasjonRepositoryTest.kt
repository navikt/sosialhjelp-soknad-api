// package no.nav.sosialhjelp.soknad.nymodell.repository
//
// import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Bosituasjon
// import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.repository.BosituasjonRepository
// import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.type.Botype
// import org.assertj.core.api.Assertions.assertThat
// import org.assertj.core.api.Assertions.assertThatThrownBy
// import org.junit.jupiter.api.Test
// import org.springframework.beans.factory.annotation.Autowired
// import org.springframework.data.relational.core.conversion.DbActionExecutionException
// import java.util.*
//
// class BosituasjonRepositoryTest : RepositoryTest() {
//
//    @Autowired
//    private lateinit var bosituasjonRepository: BosituasjonRepository
//
//    @Test
//    fun `Opprette Bosituasjon uten Soknad skal feile`() {
//        assertThatThrownBy { opprettBosituasjon(UUID.randomUUID()) }
//            .isInstanceOf(DbActionExecutionException::class.java)
//    }
//
//    @Test
//    fun `Legg til soknad og bosituasjon`() {
//        val soknad = opprettSoknad()
//        opprettBosituasjon(soknad.soknadId)
//
//        assertThat(bosituasjonRepository.findAll().size).isEqualTo(1)
//        assertThat(bosituasjonRepository.existsById(soknad.soknadId)).isTrue()
//    }
//
//    @Test
//    fun `Slett soknad som ogsa fjerner bosituasjon`() {
//        val soknad = opprettSoknad()
//
//        opprettBosituasjon(soknad.soknadId)
//        soknadRepository.delete(soknad)
//
//        assertThat(bosituasjonRepository.findAll()).isEmpty()
//    }
//
//    @Test
//    fun `Slett bosituasjon fra soknad`() {
//        opprettSoknad().run {
//            val bosituasjon = opprettBosituasjon(soknadId)
//            assertThat(bosituasjonRepository.existsById(soknadId)).isTrue()
//
//            bosituasjonRepository.delete(bosituasjon)
//            assertThat(bosituasjonRepository.existsById(soknadId)).isFalse()
//        }
//    }
//
//    @Test
//    fun `Gjentatte upserts skal fungere fint`() {
//        val soknad = opprettSoknad()
//
//        val bosituasjon = opprettBosituasjon(soknad.soknadId)
//        bosituasjon.antallPersoner = 8
//        val savedBosituasjon = bosituasjonRepository.save(bosituasjon)
//
//        assertThat(bosituasjonRepository.findById(soknad.soknadId).get().antallPersoner).isEqualTo(8)
//
//        savedBosituasjon.antallPersoner = 4
//        bosituasjonRepository.save(bosituasjon)
//
//        assertThat(bosituasjonRepository.findById(soknad.soknadId).get().antallPersoner).isEqualTo(4)
//    }
//
//    fun opprettBosituasjon(uuid: UUID) =
//        bosituasjonRepository.save(Bosituasjon(id = uuid, Botype.EIER, 4))
// }
