package no.nav.sosialhjelp.soknad.nymodell.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.jvm.optionals.getOrNull

class SoknadRepositoryTest : RepositoryTest() {

    @Test
    fun `Opprett soknad`() {
        val soknad = opprettSoknad()
        assertThat(soknadRepository.findAll().size).isEqualTo(1)

        val lagretSoknad = soknadRepository.findById(soknad.id).get()
        assertThat(lagretSoknad.id).isEqualTo(soknad.id)
    }

    @Test
    fun `Opprett og endre soknad`() {
        val soknad = opprettSoknad()
        assertThat(soknad.innsendingstidspunkt).isNull()

        val now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        soknad.innsendingstidspunkt = now

        soknadRepository.save(soknad)
        assertThat(soknadRepository.findAll().size).isEqualTo(1)

        val lagretSoknad = soknadRepository.findById(soknad.id).get()
        assertThat(lagretSoknad.innsendingstidspunkt).isEqualTo(now)
    }

    @Test
    fun `Sok pa soknad som ikke finnes`() =
        assertThat(soknadRepository.findById(UUID.randomUUID()).getOrNull()).isNull()
}
