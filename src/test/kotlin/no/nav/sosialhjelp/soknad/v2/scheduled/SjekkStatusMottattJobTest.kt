package no.nav.sosialhjelp.soknad.v2.scheduled

import com.ninjasquad.springmockk.MockkSpyBean
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import no.nav.sosialhjelp.soknad.v2.scheduled.jobs.SjekkStatusMottattJob
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadJobService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class SjekkStatusMottattJobTest : AbstractJobTest() {
    @Autowired
    private lateinit var sjekkStatusMottattJob: SjekkStatusMottattJob

    @MockkSpyBean
    private lateinit var soknadJobService: SoknadJobService

    private val capturedOutput: CapturingSlot<List<UUID>> = slot()

    @BeforeEach
    fun setup() {
        every { soknadJobService.deleteSoknaderByIds(capture(capturedOutput)) } answers { callOriginal() }
    }

    @Test
    fun `Hvis eksisterende soknad med status MOTTATT_FSL finnes skal den slettes`() {
        val metadata =
            opprettSoknadMetadata(
                status = SoknadStatus.MOTTATT_FSL,
                innsendtDato = nowWithMillis(),
            )
                .let { metadataRepository.save(it) }
        opprettSoknad(id = metadata.soknadId).also { soknadRepository.save(it) }

        sjekkStatusMottattJob.sjekkStatusMottatt()

        assertThat(metadataRepository.findByIdOrNull(metadata.soknadId)).isNotNull
        assertThat(soknadRepository.findByIdOrNull(metadata.soknadId)).isNull()
        assertThat(capturedOutput.captured).hasSize(1)
        verify(exactly = 1) { soknadJobService.deleteSoknaderByIds(capturedOutput.captured) }
    }

    @Test
    fun `Hvis det ikke eksisterer soknad med status MOTTATT_FSL skal det ikke skje noe`() {
        val metadata =
            opprettSoknadMetadata(
                status = SoknadStatus.MOTTATT_FSL,
                innsendtDato = nowWithMillis(),
            )
                .let { metadataRepository.save(it) }

        sjekkStatusMottattJob.sjekkStatusMottatt()

        assertThat(metadataRepository.findByIdOrNull(metadata.soknadId)).isNotNull
        assertThat(soknadRepository.findByIdOrNull(metadata.soknadId)).isNull()
        verify(exactly = 0) { soknadJobService.deleteSoknaderByIds(any()) }
    }
}
