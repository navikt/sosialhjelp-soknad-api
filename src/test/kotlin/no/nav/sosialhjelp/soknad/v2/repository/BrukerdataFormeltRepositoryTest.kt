package no.nav.sosialhjelp.soknad.v2.repository

import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataFormeltRepository
import no.nav.sosialhjelp.soknad.v2.createSoknadNoId
import no.nav.sosialhjelp.soknad.v2.opprettBrukerdataFormelt
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import java.util.*

class BrukerdataFormeltRepositoryTest : AbstractRepositoryTest() {

    @Autowired
    private lateinit var brukerdataFormeltRepository: BrukerdataFormeltRepository

    @Test
    fun `Skal lagre brukerdata i databasen`() {
        val soknad = createSoknadNoId().run { soknadRepository.save(this) }
        brukerdataFormeltRepository.save(opprettBrukerdataFormelt(soknad.id!!))

        assertThat(brukerdataFormeltRepository.existsById(soknad.id!!)).isTrue()
    }

    @Test
    fun `Slette soknad skal slette brukerdata`() {
        val soknad = createSoknadNoId().run { soknadRepository.save(this) }

        brukerdataFormeltRepository.save(opprettBrukerdataFormelt(soknad.id!!))
        assertThat(brukerdataFormeltRepository.existsById(soknad.id!!)).isTrue()

        soknadRepository.deleteById(soknad.id!!)
        assertThat(brukerdataFormeltRepository.existsById(soknad.id!!)).isFalse()
    }

    @Test
    fun `Opprette Brukerdata uten lagret soknad skal gi feil`() {
        assertThatThrownBy {
            opprettBrukerdataFormelt(UUID.randomUUID()).also {
                brukerdataFormeltRepository.save(it)
            }
        }.cause().isInstanceOf(DataIntegrityViolationException::class.java)
    }
}
