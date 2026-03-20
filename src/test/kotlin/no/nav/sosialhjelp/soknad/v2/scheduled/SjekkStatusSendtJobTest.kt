package no.nav.sosialhjelp.soknad.v2.scheduled

import com.ninjasquad.springmockk.MockkSpyBean
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.slot
import io.mockk.verify
import no.nav.sosialhjelp.soknad.metrics.SoknadMottattMetricsService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import no.nav.sosialhjelp.soknad.v2.scheduled.jobs.SjekkStatusSendtJob
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SjekkStatusSendtJobTest : AbstractJobTest() {
    @Autowired
    private lateinit var sjekkStatusSendtJob: SjekkStatusSendtJob

    @MockkSpyBean
    private lateinit var metricsService: SoknadMottattMetricsService

    private val capturedOutput: CapturingSlot<Int> = slot()

    @BeforeEach
    fun setup() {
        every { metricsService.setAntallGamleSoknaderStatusSendt(capture(capturedOutput)) } just Runs
        metadataRepository.deleteAll()
    }

    @Test
    fun `Soknader yngre enn 7 dager skal ikke komme med i tellingen`() {
        opprettNyereSoknad()
        opprettNyereSoknad()

        sjekkStatusSendtJob.doCheckSoknaderStatusSendt()

        verify(exactly = 1) { metricsService.setAntallGamleSoknaderStatusSendt(capturedOutput.captured) }
        assertThat(metadataRepository.findAll()).hasSize(2)
        assertThat(capturedOutput.captured).isEqualTo(0)
    }

    @Test
    fun `Soknader eldre enn 7 dager skal komme med i tellingen`() {
        opprettForGammelSoknad()
        opprettForGammelSoknad()

        sjekkStatusSendtJob.doCheckSoknaderStatusSendt()

        verify(exactly = 1) { metricsService.setAntallGamleSoknaderStatusSendt(capturedOutput.captured) }
        assertThat(metadataRepository.findAll()).hasSize(2)
        assertThat(capturedOutput.captured).isEqualTo(2)
    }

    private fun opprettForGammelSoknad() {
        opprettSoknadMetadata(
            status = SoknadStatus.SENDT,
            opprettetDato = nowMinusDays(10),
            innsendtDato = nowMinusDays(8),
        )
            .also { metadataRepository.save(it) }
    }

    private fun opprettNyereSoknad() {
        opprettSoknadMetadata(
            status = SoknadStatus.SENDT,
            opprettetDato = nowMinusDays(2),
            innsendtDato = nowMinusDays(1),
        )
            .also { metadataRepository.save(it) }
    }
}
