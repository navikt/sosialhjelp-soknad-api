package no.nav.sosialhjelp.soknad.repository

import no.nav.sosialhjelp.soknad.model.Bosituasjon
import no.nav.sosialhjelp.soknad.model.Soknad
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
        val soknad = soknadRepository.save(Soknad(soknadId = UUID.randomUUID()))
        opprettBosituasjon(soknad.soknadId)

        assertThat(bosituasjonRepository.findAll().size).isEqualTo(1)
        assertThat(bosituasjonRepository.existsBySoknadId(soknad.soknadId)).isTrue()
    }

    @Test
    fun `Slett soknad som ogsa fjerner bosituasjon`() {
        val soknad = soknadRepository.save(Soknad(soknadId = UUID.randomUUID()))

        opprettBosituasjon(soknad.soknadId)
        soknadRepository.delete(soknad)

        assertThat(bosituasjonRepository.findAll()).isEmpty()
    }

    @Test
    fun `Slett bosituasjon fra soknad`() {
        val soknad = soknadRepository.save(Soknad(soknadId = UUID.randomUUID()))
        val bosituasjon = bosituasjonRepository.save(Bosituasjon(soknadId = soknad.soknadId))

        assertThat(bosituasjonRepository.existsBySoknadId(soknad.soknadId)).isTrue()

        bosituasjonRepository.delete(bosituasjon)

        assertThat(bosituasjonRepository.existsBySoknadId(soknad.soknadId)).isFalse()
    }

    @Test
    fun `Ved oppdatering fra frontent, lagre Bosituasjon selvom den finnes skal gi feil`() {
        val soknad = opprettSoknad()
        // oppretter for første gang
        opprettBosituasjon(soknad.soknadId)

        // oppretter for andre gang skal gi feil
        assertThatThrownBy {
            opprettBosituasjon(soknad.soknadId)
        }.isInstanceOf(DbActionExecutionException::class.java)
    }

    fun opprettBosituasjon(uuid: UUID) = bosituasjonRepository.save(Bosituasjon(soknadId = uuid))
}