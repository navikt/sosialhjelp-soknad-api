package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.test.runTest
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.scheduled.jobs.SlettGamleSoknaderMedStatusSendtJob
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.UUID

class SlettGamleSoknaderMedStatusSendtJobTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var slettGamleSoknaderMedStatusSendtJob: SlettGamleSoknaderMedStatusSendtJob

    @BeforeEach
    fun setup() {
        soknadRepository.deleteAll()
        metadataRepository.deleteAll()
    }

    @Test
    fun `Planlagt jobb skal slette soknader med status sendt eldre enn 7 dager`() =
        runTest {
            val soknadId = createMetadataAndSoknad(nowWithMillis().minusDays(5))
            createMetadataAndSoknad(nowWithMillis().minusDays(10))
            createMetadataAndSoknad(nowWithMillis().minusDays(20))

            slettGamleSoknaderMedStatusSendtJob.slettSoknader()

            soknadRepository.findAll().also { soknader ->
                assertThat(soknader)
                    .hasSize(1)
                    .allMatch { it.id == soknadId }
            }
        }

    @Test
    fun `Skal kun slette soknader med status SENDT`() =
        runTest {
            val slettetId = createMetadataAndSoknad(nowWithMillis().minusDays(15))
            createMetadataAndSoknad(nowWithMillis().minusDays(15), status = SoknadStatus.OPPRETTET)
            createMetadataAndSoknad(nowWithMillis().minusDays(15), status = SoknadStatus.MOTTATT_FSL)

            slettGamleSoknaderMedStatusSendtJob.slettSoknader()

            soknadRepository.findAll().also { soknader ->
                assertThat(soknader)
                    .hasSize(2)
                    .noneMatch { it.id == slettetId }
            }
        }

    private fun createMetadataAndSoknad(
        opprettet: LocalDateTime,
        status: SoknadStatus = SoknadStatus.SENDT,
    ): UUID {
        val soknadId = metadataRepository.createMetadata(opprettet, status)
        opprettSoknad(id = soknadId).also { soknadRepository.save(it) }

        return soknadId
    }
}
