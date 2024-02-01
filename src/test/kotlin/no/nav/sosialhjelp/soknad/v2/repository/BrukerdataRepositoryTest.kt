package no.nav.sosialhjelp.soknad.v2.repository

import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataRepository
import no.nav.sosialhjelp.soknad.v2.createSoknadNoId
import no.nav.sosialhjelp.soknad.v2.opprettBrukerdata
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import java.util.*

class BrukerdataRepositoryTest : AbstractRepositoryTest() {

    @Autowired
    private lateinit var brukerdataRepository: BrukerdataRepository

    @Test
    fun `Lagre brukerdata skal fungere`() {
        val soknad = createSoknadNoId().run { soknadRepository.save(this) }
        brukerdataRepository.save(opprettBrukerdata(soknad.id!!))

        assertThat(brukerdataRepository.existsById(soknad.id!!)).isTrue()
    }

    @Test
    fun `Slette soknad skal slette brukerdata`() {
        val soknad = createSoknadNoId().run { soknadRepository.save(this) }

        brukerdataRepository.save(opprettBrukerdata(soknad.id!!))
        assertThat(brukerdataRepository.existsById(soknad.id!!)).isTrue()

        soknadRepository.deleteById(soknad.id!!)
        assertThat(brukerdataRepository.existsById(soknad.id!!)).isFalse()
    }

    @Test
    fun `Opprette Brukerdata uten lagret soknad skal gi feil`() {
        assertThatThrownBy {
            opprettBrukerdata(UUID.randomUUID()).also {
                brukerdataRepository.save(it)
            }
        }.cause().isInstanceOf(DataIntegrityViolationException::class.java)
    }
}
