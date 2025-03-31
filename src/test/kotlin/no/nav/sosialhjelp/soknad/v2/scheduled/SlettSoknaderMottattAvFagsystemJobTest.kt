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
import no.nav.sosialhjelp.soknad.v2.scheduled.jobs.SlettSoknaderMottattAvFagsystemJob
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime
import java.util.UUID

class SlettSoknaderMottattAvFagsystemJobTest : AbstractIntegrationTest() {
    @MockkBean
    private lateinit var digisosApiV2Client: DigisosApiV2Client

    @Autowired
    private lateinit var slettMottatteSoknaderJob: SlettSoknaderMottattAvFagsystemJob

    @BeforeEach
    fun setUp() {
        metadataRepository.deleteAll()
        soknadRepository.deleteAll()
    }

    @Test
    fun `Skal slette soknader som er registrert mottatt av fagsystem`() =
        runTest {
            val soknadMetadata =
                opprettSoknadMetadata(lagretSoknadId, status = SoknadStatus.SENDT, innsendtDato = LocalDateTime.now())
            soknadMetadata
                .let { metadataRepository.save(it) }

            assertThat(soknadRepository.findByIdOrNull(lagretSoknadId)).isNotNull()
            assertThat(metadataRepository.findByIdOrNull(lagretSoknadId)).isNotNull()

            every { digisosApiV2Client.getStatusForSoknader(any()) } returns
                createFiksSoknadStatusListe(soknadMetadata.digisosId!!)

            slettMottatteSoknaderJob.slettSoknaderSomErMottattAvFagsystem()

            assertThat(soknadRepository.findByIdOrNull(lagretSoknadId)).isNull()
            assertThat(metadataRepository.findByIdOrNull(lagretSoknadId)!!.status).isEqualTo(SoknadStatus.MOTTATT_FSL)
        }

    @Test
    fun `Skal ikke slette soknader som ikke er registrert mottatt av fagsystem`() =
        runTest {
            val metadata =
                opprettSoknadMetadata(
                    status = SoknadStatus.SENDT,
                    innsendtDato = LocalDateTime.now(),
                )
                    .let { metadataRepository.save(it) }
            val lagretSoknadId = opprettSoknad(id = metadata.soknadId).let { soknadRepository.save(it).id }

            assertThat(soknadRepository.findById(lagretSoknadId)).isNotEmpty
            assertThat(metadataRepository.findById(lagretSoknadId)).isNotEmpty

            every { digisosApiV2Client.getStatusForSoknader(any()) } returns createEmptyFiksSoknadStatusListe()

            slettMottatteSoknaderJob.slettSoknaderSomErMottattAvFagsystem()

            assertThat(soknadRepository.findById(lagretSoknadId)).isNotEmpty
            assertThat(metadataRepository.findById(lagretSoknadId).get().status).isEqualTo(SoknadStatus.SENDT)
        }

    @Test
    fun `Skal kun sjekke status hos FIKS for soknader som er sendt`() =
        runTest {
            val metadata =
                opprettSoknadMetadata(status = SoknadStatus.OPPRETTET)
                    .let { metadataRepository.save(it) }
            val lagretSoknadId = opprettSoknad(id = metadata.soknadId).let { soknadRepository.save(it).id }

            assertThat(soknadRepository.findById(lagretSoknadId)).isNotEmpty
            assertThat(metadataRepository.findById(lagretSoknadId)).isNotEmpty

            slettMottatteSoknaderJob.slettSoknaderSomErMottattAvFagsystem()

            verify(exactly = 0) { digisosApiV2Client.getStatusForSoknader(any()) }

            assertThat(soknadRepository.findById(lagretSoknadId)).isNotEmpty
            assertThat(metadataRepository.findById(lagretSoknadId).get().status).isEqualTo(SoknadStatus.OPPRETTET)
        }

    private fun createFiksSoknadStatusListe(digisosId: UUID): FiksSoknadStatusListe =
        FiksSoknadStatusListe(
            listOf(
                FiksSoknadStatus(digisosId, true),
            ),
        )

    private fun createEmptyFiksSoknadStatusListe(): FiksSoknadStatusListe = FiksSoknadStatusListe(emptyList())
}
