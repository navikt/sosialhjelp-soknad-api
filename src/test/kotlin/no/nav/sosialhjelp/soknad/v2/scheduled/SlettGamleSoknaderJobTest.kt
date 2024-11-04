package no.nav.sosialhjelp.soknad.v2.scheduled

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kotlinx.coroutines.test.runTest
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringClient
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

class SlettGamleSoknaderJobTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var slettGamleSoknaderJob: SlettGamleSoknaderJob

    @MockkBean(relaxed = true)
    private lateinit var mellomlagringClient: MellomlagringClient

    @BeforeEach
    fun setup() {
        soknadRepository.deleteAll()
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns
            MellomlagringDto("", emptyList())
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

    @Test
    fun `Ved feil hos FIKS skal soknader lokalt fortsatt slettes`() {
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } throws RuntimeException("Feil hos FIKS")

        runTest(timeout = 5.seconds) {
            soknadRepository.save(opprettSoknad(opprettet = LocalDateTime.now().minusDays(15)))
            soknadRepository.save(opprettSoknad(opprettet = LocalDateTime.now().minusDays(15)))

            slettGamleSoknaderJob.slettGamleSoknader()

            assertThat(soknadRepository.findAll()).isEmpty()
        }
    }
}
