package no.nav.sosialhjelp.soknad.v2.scheduled.slettgamlesoknader

import kotlinx.coroutines.test.runTest
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.scheduled.SlettGamleSoknaderJob
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

class SlettGamleSoknaderJobTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var slettGamleSoknaderJob: SlettGamleSoknaderJob

    @BeforeEach
    fun setup() {
        soknadRepository.deleteAll()
    }

    @Test
    fun `planlagt jobb skal slette soknader eldre enn 14 dager`() =
        runTest(timeout = 5.seconds) {
            soknadRepository.save(opprettSoknad(opprettet = LocalDateTime.now().minusDays(15)))

            slettGamleSoknaderJob.slettGamleSoknader()

            assertThat(soknadRepository.findAll()).isEmpty()
        }

    @Test
    fun `Planlagt jobb skal ikke slette soknader nyere enn 14 dager`() =
        runTest(timeout = 5.seconds) {
            soknadRepository.save(opprettSoknad(opprettet = LocalDateTime.now().minusDays(13)))

            slettGamleSoknaderJob.slettGamleSoknader()

            assertThat(soknadRepository.findAll()).isNotEmpty()
        }
}
