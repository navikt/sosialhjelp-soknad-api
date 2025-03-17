package no.nav.sosialhjelp.soknad.v2.scheduled

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kotlinx.coroutines.test.runTest
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringClient
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class SlettGamleSoknaderJobTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var slettGamleSoknaderJob: SlettGamleSoknaderJob

    @MockkBean(relaxed = true)
    private lateinit var mellomlagringClient: MellomlagringClient

    @BeforeEach
    fun setup() {
        soknadRepository.deleteAll()
        every { mellomlagringClient.hentDokumenterMetadata(any()) } returns
            MellomlagringDto("", emptyList())
    }

    @Test
    fun `planlagt jobb skal slette soknader eldre enn 14 dager`() =
        runTest(timeout = 5.seconds) {
            val soknadId = soknadMetadataRepository.createMetadata(LocalDateTime.now().minusDays(15))
            soknadRepository.save(opprettSoknad(id = soknadId))

            slettGamleSoknaderJob.slettGamleSoknader()

            assertThat(soknadRepository.findAll()).isEmpty()
        }

    @Test
    fun `Planlagt jobb skal ikke slette soknader nyere enn 14 dager`() =
        runTest(timeout = 5.seconds) {
            soknadRepository.save(opprettSoknad())

            slettGamleSoknaderJob.slettGamleSoknader()

            assertThat(soknadRepository.findAll()).isNotEmpty()
        }

    @Test
    fun `Ved feil hos FIKS skal soknader lokalt fortsatt slettes`() {
        every { mellomlagringClient.hentDokumenterMetadata(any()) } throws RuntimeException("Feil hos FIKS")

        runTest(timeout = 5.seconds) {
            val soknadId1 = soknadMetadataRepository.createMetadata(LocalDateTime.now().minusDays(15))
            soknadRepository.save(opprettSoknad(id = soknadId1))
            val soknadId2 = soknadMetadataRepository.createMetadata(LocalDateTime.now().minusDays(15))
            soknadRepository.save(opprettSoknad(id = soknadId2))

            slettGamleSoknaderJob.slettGamleSoknader()

            assertThat(soknadRepository.findAll()).isEmpty()
        }
    }
}

private fun SoknadMetadataRepository.createMetadata(opprettet: LocalDateTime): UUID {
    return SoknadMetadata(
        soknadId = UUID.randomUUID(),
        personId = "12345612345",
        tidspunkt = Tidspunkt(opprettet = opprettet),
    )
        .also { save(it) }
        .soknadId
}
