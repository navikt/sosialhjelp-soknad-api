package no.nav.sosialhjelp.soknad.v2.repository

import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRepository
import no.nav.sosialhjelp.soknad.v2.opprettLivssituasjon
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class LivsituasjonRepositoryTest : AbstractRepositoryTest() {

    @Autowired
    private lateinit var livssituasjonRepository: LivssituasjonRepository

    @Test
    fun `Skal lagre Livssituasjon`() {
        opprettLivssituasjon(soknad.id).also { livssituasjonRepository.save(it) }
        assertThat(livssituasjonRepository.existsById(soknad.id)).isTrue()
    }
}
