package no.nav.sosialhjelp.soknad.v2.repository

import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import no.nav.sosialhjelp.soknad.v2.opprettEier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class EierRepositoryTest : AbstractRepositoryTest() {

    @Autowired
    private lateinit var eierRepository: EierRepository

    @Test
    fun `Skal lagre Eier`() {
        eierRepository.save(opprettEier(soknad.id, soknad.eierPersonId))
        assertThat(eierRepository.existsById(soknad.id)).isTrue()
    }

    @Test
    fun `Hente eiers personId skal returnere eierPersonId fra Soknad`() {
        val eier = eierRepository.save(opprettEier(soknad.id))
        assertThat(eierRepository.getEierPersonId(eier.soknadId)).isEqualTo(soknad.eierPersonId)
    }
}
