package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.test.runTest
import no.nav.sosialhjelp.soknad.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.INNSENDING_FEILET
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.MOTTATT_FSL
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.OPPRETTET
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.SENDT
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.scheduled.jobs.SlettGamleSoknaderJob
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

    @BeforeEach
    fun setup() {
        soknadRepository.deleteAll()
        soknadMetadataRepository.deleteAll()
    }

    @Test
    fun `Planlagt jobb skal slette soknader eldre enn 14 dager`() =
        runTest(timeout = 5.seconds) {
            val soknadId = soknadMetadataRepository.createMetadata(LocalDateTime.now().minusDays(15))
            soknadRepository.save(opprettSoknad(id = soknadId))

            slettGamleSoknaderJob.slettGamleSoknader()

            assertThat(soknadRepository.findAll()).isEmpty()
            assertThat(soknadMetadataRepository.findAll()).isEmpty()
        }

    @Test
    fun `Planlagt jobb skal ikke slette soknader nyere enn 14 dager`() =
        runTest(timeout = 5.seconds) {
            val soknadId = soknadMetadataRepository.createMetadata(LocalDateTime.now().minusDays(10))
            soknadRepository.save(opprettSoknad(id = soknadId))

            slettGamleSoknaderJob.slettGamleSoknader()

            assertThat(soknadRepository.findAll()).hasSize(1)
            assertThat(soknadMetadataRepository.findAll()).hasSize(1).allMatch { it.status == OPPRETTET }
        }

    @Test
    fun `Skal kun slette soknader som ikke har status SENDT eller MOTTATT_FSL`() {
        runTest {
            val ids =
                listOf(
                    createMetadataAndSoknad(LocalDateTime.now().minusDays(15), SENDT),
                    createMetadataAndSoknad(LocalDateTime.now().minusDays(15), MOTTATT_FSL),
                    createMetadataAndSoknad(LocalDateTime.now().minusDays(15), OPPRETTET),
                )

            slettGamleSoknaderJob.slettGamleSoknader()

            val allSoknader = soknadRepository.findAllById(ids)
            assertThat(allSoknader).hasSize(2)

            val metadatas = soknadMetadataRepository.findAllById(ids)
            assertThat(metadatas)
                .hasSize(2)
                .anyMatch { it.status == SENDT }
                .anyMatch { it.status == MOTTATT_FSL }
                .noneMatch { it.status == OPPRETTET }
        }
    }

    @Test
    fun `Skal ikke slette soknader med SENDING_FEILET`() =
        runTest(timeout = 5.seconds) {
            val ids =
                listOf(
                    createMetadataAndSoknad(nowWithMillis().minusDays(15), INNSENDING_FEILET),
                    createMetadataAndSoknad(nowWithMillis().minusDays(15), OPPRETTET),
                )

            slettGamleSoknaderJob.slettGamleSoknader()

            soknadRepository.findAllById(ids)
                .also { assertThat(it).hasSize(1) }

            soknadMetadataRepository.findAllById(ids)
                .also { metadata ->
                    assertThat(metadata)
                        .hasSize(1)
                        .anyMatch { it.status == INNSENDING_FEILET }
                }
        }

    private fun createMetadataAndSoknad(
        opprettet: LocalDateTime,
        status: SoknadStatus,
    ): UUID {
        val soknadId = soknadMetadataRepository.createMetadata(opprettet, status)
        opprettSoknad(id = soknadId).also { soknadRepository.save(it) }

        return soknadId
    }
}

fun SoknadMetadataRepository.createMetadata(
    opprettet: LocalDateTime,
    status: SoknadStatus = OPPRETTET,
    sendtInn: LocalDateTime = LocalDateTime.now(),
    personId: String = "12345612345",
): UUID {
    return SoknadMetadata(
        soknadId = UUID.randomUUID(),
        personId = personId,
        tidspunkt = Tidspunkt(opprettet = opprettet, sendtInn = sendtInn),
        status = status,
    )
        .also { save(it) }
        .soknadId
}
