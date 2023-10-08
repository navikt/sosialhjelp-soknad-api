package no.nav.sosialhjelp.soknad.repository

import no.nav.sosialhjelp.soknad.model.Soknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import kotlin.jvm.optionals.getOrNull

class SoknadRepositoryTest : RepositoryTest() {

    @Test
    fun testSaveOppgave() {
        val soknad = soknadRepository.save(Soknad(soknadId = UUID.randomUUID()))

        assertThat(soknadRepository.findAll().size).isEqualTo(1)
        val lagretSoknad = soknadRepository.findBySoknadId(soknad.soknadId).get()
        assertThat(lagretSoknad.soknadId).isEqualTo(soknad.soknadId)
        assertThat(soknadRepository.existsById(soknad.id)).isEqualTo(true)
    }

    @Test
    fun `Sok pa soknad som ikke finnes`() =
        assertThat(soknadRepository.findBySoknadId(UUID.randomUUID()).getOrNull()).isNull()
}