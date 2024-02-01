package no.nav.sosialhjelp.soknad.v2.repository

import no.nav.sosialhjelp.soknad.v2.createSoknadNoId
import no.nav.sosialhjelp.soknad.v2.opprettEier
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SoknadRepositoryTest : AbstractRepositoryTest() {

    @Test
    fun `Lagre ny soknad`() {
        soknadRepository.save(createSoknadNoId()).also {
            assertThat(it.id).isNotNull()
        }
    }

    @Test
    fun `Oppdatere soknad`() {
        val lagretSoknad = soknadRepository.save(createSoknadNoId())

        val oppdatertSoknad = Soknad(
            id = lagretSoknad.id,
            innsendingstidspunkt = lagretSoknad.innsendingstidspunkt,
            eier = opprettEier(personId = "0987654321")
        ).also {
            soknadRepository.save(it)
        }

        assertThat(oppdatertSoknad.id).isEqualTo(lagretSoknad.id)
        assertThat(oppdatertSoknad.innsendingstidspunkt).isEqualTo(lagretSoknad.innsendingstidspunkt)
        assertThat(oppdatertSoknad.eier.personId).isNotEqualTo(lagretSoknad.eier.personId)
    }
}
