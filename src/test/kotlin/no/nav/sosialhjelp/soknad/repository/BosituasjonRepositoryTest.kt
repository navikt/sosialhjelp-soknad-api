package no.nav.sosialhjelp.soknad.repository

import no.nav.sosialhjelp.soknad.domene.soknad.Bosituasjon
import no.nav.sosialhjelp.soknad.domene.soknad.BosituasjonRepository
import no.nav.sosialhjelp.soknad.domene.soknad.Botype
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import java.util.*

class BosituasjonRepositoryTest : RepositoryTest() {

    @Autowired
    private lateinit var bosituasjonRepository: BosituasjonRepository

    @Test
    fun `Opprette Bosituasjon uten Soknad skal feile`() {
        assertThatThrownBy { opprettBosituasjon(UUID.randomUUID()) }
            .isInstanceOf(DbActionExecutionException::class.java)
    }

    @Test
    fun `Legg til soknad og bosituasjon`() {
        val soknad = opprettSoknad()
        opprettBosituasjon(soknad.id)

        assertThat(bosituasjonRepository.findAll().size).isEqualTo(1)
        assertThat(bosituasjonRepository.existsById(soknad.id)).isTrue()
    }

    @Test
    fun `Slett soknad som ogsa fjerner bosituasjon`() {
        val soknad = opprettSoknad()

        opprettBosituasjon(soknad.id)
        soknadRepository.delete(soknad)

        assertThat(bosituasjonRepository.findAll()).isEmpty()
    }

    @Test
    fun `Slett bosituasjon fra soknad`() {
        opprettSoknad().run {
            val bosituasjon = opprettBosituasjon(id)
            assertThat(bosituasjonRepository.existsById(id)).isTrue()

            bosituasjonRepository.delete(bosituasjon)
            assertThat(bosituasjonRepository.existsById(id)).isFalse()
        }
    }

    @Test
    fun `Gjentatte upserts skal fungere fint`() {
        val soknad = opprettSoknad()

        val bosituasjon = opprettBosituasjon(soknad.id)
        bosituasjon.antallPersoner = 8
        val savedBosituasjon = bosituasjonRepository.save(bosituasjon)

        assertThat(bosituasjonRepository.findById(soknad.id).get().antallPersoner).isEqualTo(8)

        savedBosituasjon.antallPersoner = 4
        bosituasjonRepository.save(bosituasjon)

        assertThat(bosituasjonRepository.findById(soknad.id).get().antallPersoner).isEqualTo(4)
    }

    fun opprettBosituasjon(uuid: UUID) =
        bosituasjonRepository.save(Bosituasjon(soknadId = uuid, Botype.EIER, 4))
}