package no.nav.sosialhjelp.soknad.v2.scheduled

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiV2Client
import no.nav.sosialhjelp.soknad.innsending.digisosapi.FiksSoknadStatus
import no.nav.sosialhjelp.soknad.innsending.digisosapi.FiksSoknadStatusListe
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.UUID

class SlettMottatteSoknaderJobTest : AbstractIntegrationTest() {
    @MockkBean
    private lateinit var digisosApiV2Client: DigisosApiV2Client

    @Autowired
    private lateinit var slettMottatteSoknaderJob: SlettMottatteSoknaderJob

    @Test
    fun `Skal slette soknader som er registrert mottatt av fagsystem`() =
        runTest {
            val lagretSoknadId = opprettSoknad().let { soknadRepository.save(it).id }
            opprettSoknadMetadata(lagretSoknadId, status = SoknadStatus.SENDT, innsendtDato = LocalDateTime.now())
                .let { soknadMetadataRepository.save(it) }

            assertThat(soknadRepository.findById(lagretSoknadId)).isNotEmpty
            assertThat(soknadMetadataRepository.findById(lagretSoknadId)).isNotEmpty

            every { digisosApiV2Client.getStatusForSoknader(any()) } returns createFiksSoknadStatusListe(lagretSoknadId)

            slettMottatteSoknaderJob.slettSoknaderSomErMottattAvFagsystem()

            assertThat(soknadRepository.findById(lagretSoknadId)).isEmpty
            assertThat(soknadMetadataRepository.findById(lagretSoknadId).get().status).isEqualTo(SoknadStatus.MOTTATT_FSL)
        }

    @Test
    fun `Skal ikke slette soknader som ikke er registrert mottatt av fagsystem`() =
        runTest {
            val lagretSoknadId = opprettSoknad().let { soknadRepository.save(it).id }
            opprettSoknadMetadata(
                lagretSoknadId,
                status = SoknadStatus.SENDT,
                innsendtDato = LocalDateTime.now(),
            ).let { soknadMetadataRepository.save(it) }

            assertThat(soknadRepository.findById(lagretSoknadId)).isNotEmpty
            assertThat(soknadMetadataRepository.findById(lagretSoknadId)).isNotEmpty

            every { digisosApiV2Client.getStatusForSoknader(any()) } returns createEmptyFiksSoknadStatusListe()

            slettMottatteSoknaderJob.slettSoknaderSomErMottattAvFagsystem()

            assertThat(soknadRepository.findById(lagretSoknadId)).isNotEmpty
            assertThat(soknadMetadataRepository.findById(lagretSoknadId).get().status).isEqualTo(SoknadStatus.SENDT)
        }

    @Test
    fun `Skal kun sjekke status hos FIKS for soknader som er sendt`() =
        runTest {
            val lagretSoknadId = opprettSoknad().let { soknadRepository.save(it).id }
            opprettSoknadMetadata(lagretSoknadId, status = SoknadStatus.OPPRETTET)
                .let { soknadMetadataRepository.save(it) }

            assertThat(soknadRepository.findById(lagretSoknadId)).isNotEmpty
            assertThat(soknadMetadataRepository.findById(lagretSoknadId)).isNotEmpty

            slettMottatteSoknaderJob.slettSoknaderSomErMottattAvFagsystem()

            verify(exactly = 0) { digisosApiV2Client.getStatusForSoknader(any()) }

            assertThat(soknadRepository.findById(lagretSoknadId)).isNotEmpty
            assertThat(soknadMetadataRepository.findById(lagretSoknadId).get().status).isEqualTo(SoknadStatus.OPPRETTET)
        }

    private fun createFiksSoknadStatusListe(lagretSoknadId: UUID): FiksSoknadStatusListe =
        FiksSoknadStatusListe(
            listOf(
                FiksSoknadStatus(lagretSoknadId, true),
            ),
        )

    private fun createEmptyFiksSoknadStatusListe(): FiksSoknadStatusListe = FiksSoknadStatusListe(emptyList())
}
