package no.nav.sosialhjelp.soknad.v2.repository

import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SoknadRepositoryTest : AbstractRepositoryTest() {

    @Test
    fun `Skal lagre ny soknad i databasen`() {
        soknadRepository.save(opprettSoknad()).also {
            assertThat(it.id).isNotNull()
        }
    }

    @Test
    fun `Skal oppdatere soknad i databasen`() {
        val lagretSoknad = soknadRepository.save(opprettSoknad())

        val oppdatertSoknad = lagretSoknad.copy(
            eierPersonId = "Noe helt annet"
        ).also { soknadRepository.save(it) }

        assertThat(oppdatertSoknad.id).isEqualTo(lagretSoknad.id)
        assertThat(oppdatertSoknad.tidspunkt.opprettet).isEqualTo(lagretSoknad.tidspunkt.opprettet)
        assertThat(oppdatertSoknad.eierPersonId).isNotEqualTo(lagretSoknad.eierPersonId)
    }
}
