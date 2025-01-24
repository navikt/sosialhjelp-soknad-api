package no.nav.sosialhjelp.soknad.v2.scheduled

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kotlinx.coroutines.test.runTest
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

class SlettSoknaderSomErMottattAvFagsystemJobTest : AbstractIntegrationTest() {
    @MockkBean
    private lateinit var digisosApiService: DigisosApiService

    @Autowired
    private lateinit var slettSoknaderSomErMottattAvFagsystemJob: SlettSoknaderSomErMottattAvFagsystemJob

    @Test
    fun `Skal slette soknader som er registrert mottatt av fagsystem`() =
        runTest {
            val lagretSoknadId = opprettSoknad().let { soknadRepository.save(it).id }
            opprettSoknadMetadata(lagretSoknadId, status = SoknadStatus.SENDT, innsendtDato = LocalDateTime.now()).let { soknadMetadataRepository.save(it) }

            assertThat(soknadRepository.findById(lagretSoknadId)).isNotEmpty
            assertThat(soknadMetadataRepository.findById(lagretSoknadId)).isNotEmpty

            every { digisosApiService.getSoknaderMedStatusMotattFagsystem(any()) } returns listOf(lagretSoknadId)

            slettSoknaderSomErMottattAvFagsystemJob.slettSoknaderSomErMottattAvFagsystem()

            assertThat(soknadRepository.findById(lagretSoknadId)).isEmpty
            assertThat(soknadMetadataRepository.findById(lagretSoknadId).get().status).isEqualTo(SoknadStatus.MOTTATT_FSL)
        }
}
