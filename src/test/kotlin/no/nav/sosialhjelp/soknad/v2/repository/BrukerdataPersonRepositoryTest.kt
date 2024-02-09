package no.nav.sosialhjelp.soknad.v2.repository

import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataPersonRepository
import no.nav.sosialhjelp.soknad.v2.createSoknad
import no.nav.sosialhjelp.soknad.v2.opprettBrukerdataPerson
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import java.util.*

class BrukerdataPersonRepositoryTest : AbstractRepositoryTest() {

    @Autowired
    private lateinit var brukerdataPersonRepository: BrukerdataPersonRepository

    @Test
    fun `Skal lagre brukerdata i databasen`() {
        val soknad = soknadRepository.save(createSoknad())
        brukerdataPersonRepository.save(opprettBrukerdataPerson(soknad.id))

        Assertions.assertThat(brukerdataPersonRepository.existsById(soknad.id)).isTrue()
    }

    @Test
    fun `Slette soknad skal slette brukerdata`() {
        val soknad = soknadRepository.save(createSoknad())

        brukerdataPersonRepository.save(opprettBrukerdataPerson(soknad.id))
        Assertions.assertThat(brukerdataPersonRepository.existsById(soknad.id)).isTrue()

        soknadRepository.deleteById(soknad.id)
        Assertions.assertThat(brukerdataPersonRepository.existsById(soknad.id)).isFalse()
    }

    @Test
    fun `Opprette Brukerdata uten lagret soknad skal gi feil`() {
        Assertions.assertThatThrownBy {
            opprettBrukerdataPerson(UUID.randomUUID()).also {
                brukerdataPersonRepository.save(it)
            }
        }.cause().isInstanceOf(DataIntegrityViolationException::class.java)
    }
}
