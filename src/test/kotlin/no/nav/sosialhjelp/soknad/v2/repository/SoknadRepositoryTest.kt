package no.nav.sosialhjelp.soknad.v2.repository

import no.nav.sosialhjelp.soknad.v2.createSoknad
import no.nav.sosialhjelp.soknad.v2.opprettEier
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.Tidspunkt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SoknadRepositoryTest : AbstractRepositoryTest() {

    @Test
    fun `Skal lagre ny soknad i databasen`() {
        soknadRepository.save(createSoknad()).also {
            assertThat(it.id).isNotNull()
        }
    }

    @Test
    fun `Skal oppdatere soknad i databasen`() {
        val lagretSoknad = soknadRepository.save(createSoknad())

        val oppdatertSoknad = Soknad(
            id = lagretSoknad.id,
            tidspunkt = Tidspunkt(opprettet = lagretSoknad.tidspunkt.opprettet),
            eier = opprettEier(personId = "0987654321")
        ).also {
            soknadRepository.save(it)
        }

        assertThat(oppdatertSoknad.id).isEqualTo(lagretSoknad.id)
        assertThat(oppdatertSoknad.tidspunkt.opprettet).isEqualTo(lagretSoknad.tidspunkt.opprettet)
        assertThat(oppdatertSoknad.eier.personId).isNotEqualTo(lagretSoknad.eier.personId)
    }
}
