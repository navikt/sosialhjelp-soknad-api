package no.nav.sosialhjelp.soknad.v2.repository

import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.opprettKontakt
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import java.util.*

class KontaktRepositoryTest : AbstractRepositoryTest() {

    @Autowired
    private lateinit var kontaktRepository: KontaktRepository

    @Test
    fun `Skal lagre kontaktinformasjon`() {
        opprettKontakt(soknad.id).also { kontaktRepository.save(it) }
        assertThat(kontaktRepository.existsById(soknad.id)).isTrue()
    }

    @Test
    fun `Slette soknad skal slette kontaktinformasjon`() {
        opprettKontakt(soknad.id).also { kontaktRepository.save(it) }
        assertThat(kontaktRepository.existsById(soknad.id)).isTrue()

        soknadRepository.deleteById(soknad.id)
        assertThat(kontaktRepository.existsById(soknad.id)).isFalse()
    }

    @Test
    fun `Opprette kontaktinformasjon uten soknad skal gi feil`() {
        assertThatThrownBy {
            opprettKontakt(UUID.randomUUID()).also { kontaktRepository.save(it) }
        }.cause().isInstanceOf(DataIntegrityViolationException::class.java)
    }
}
