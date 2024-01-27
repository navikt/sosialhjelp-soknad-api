package no.nav.sosialhjelp.soknad.v2.repository

import no.nav.sosialhjelp.soknad.v2.adresse.AdresseRepository
import no.nav.sosialhjelp.soknad.v2.createSoknad
import no.nav.sosialhjelp.soknad.v2.opprettAdresserSoknad
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import java.util.*

class AdresseRepositoryTest: AbstractRepositoryTest() {

    @Autowired
    private lateinit var adresseRepository: AdresseRepository

    @Test
    fun `Lagre adresser for soknad`() {
        val soknad = createSoknad().run { soknadRepository.save(this) }

        opprettAdresserSoknad(soknadId = soknad.id!!).also { adresseRepository.save(it) }

        assertThat(adresseRepository.existsById(soknad.id!!)).isTrue()
    }

    @Test
    fun `Slette soknad skal slette AdresseSoknad`() {
        val soknad = createSoknad().run { soknadRepository.save(this) }

        opprettAdresserSoknad(soknadId = soknad.id!!).also { adresseRepository.save(it) }
        assertThat(adresseRepository.existsById(soknad.id!!)).isTrue()

        soknadRepository.deleteById(soknad.id!!)
        assertThat(adresseRepository.existsById(soknad.id!!)).isFalse()
    }

    @Test
    fun `Opprette Adresser uten soknad skal gi feil`() {
        assertThatThrownBy {
            opprettAdresserSoknad(UUID.randomUUID()).also { adresseRepository.save(it) }
        }.cause().isInstanceOf(DataIntegrityViolationException::class.java)
    }
}
