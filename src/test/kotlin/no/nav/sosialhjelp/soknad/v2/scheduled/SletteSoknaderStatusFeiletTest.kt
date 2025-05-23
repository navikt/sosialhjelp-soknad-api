package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.test.runTest
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.INNSENDING_FEILET
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.MOTTATT_FSL
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.OPPRETTET
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.SENDT
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.scheduled.jobs.SletteSoknaderStatusFeiletJob
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class SletteSoknaderStatusFeiletTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var sletteJob: SletteSoknaderStatusFeiletJob

    @BeforeEach
    fun setUp() {
        soknadRepository.deleteAll()
        metadataRepository.deleteAll()
    }

    @Test
    fun `Soknader som feilet ved innsending skal slettes etter 5 ekstra dager`() =
        runTest(timeout = 5.seconds) {
            createMetadataAndSoknad(nowMinusDays(20L), INNSENDING_FEILET)

            sletteJob.sletteSoknaderStatusFeilet()

            soknadRepository.findAll().also { assertThat(it).isEmpty() }
            metadataRepository.findAll()
                .also { metadata -> assertThat(metadata).hasSize(1).allMatch { it.status == INNSENDING_FEILET } }
        }

    @Test
    fun `Soknad med status feilet yngre enn 14 + 5 dager skal ikke slettes`() =
        runTest(timeout = 5.seconds) {
            createMetadataAndSoknad(nowMinusDays(14), INNSENDING_FEILET)

            sletteJob.sletteSoknaderStatusFeilet()

            soknadRepository.findAll().also { assertThat(it).hasSize(1) }
            metadataRepository.findAll()
                .also { metadata -> assertThat(metadata).hasSize(1).allMatch { it.status == INNSENDING_FEILET } }
        }

    @Test
    fun `Slette-jobb skal kun ta stilling til INNSENDING_FEILET`() =
        runTest(timeout = 5.seconds) {
            createMetadataAndSoknad(nowMinusDays(20), OPPRETTET)
            createMetadataAndSoknad(nowMinusDays(20), SENDT)
            createMetadataAndSoknad(nowMinusDays(20), MOTTATT_FSL)
            createMetadataAndSoknad(opprettet = nowMinusDays(25), INNSENDING_FEILET, sendtInn = nowMinusDays(20))

            sletteJob.sletteSoknaderStatusFeilet()

            soknadRepository.findAll()
                .also { assertThat(it).hasSize(3) }
                .map { it.id }
                .forEach { id ->
                    metadataRepository.findByIdOrNull(id)!!
                        .also {
                            assertThat(it).isNotNull()
                            assertThat(it.status).isNotEqualTo(INNSENDING_FEILET)
                        }
                }

            assertThat(metadataRepository.findAll())
                .hasSize(4)
                .anyMatch { it.status == INNSENDING_FEILET }
        }

    private fun createMetadataAndSoknad(
        opprettet: LocalDateTime,
        status: SoknadStatus,
        sendtInn: LocalDateTime = LocalDateTime.now(),
    ): UUID {
        val soknadId = metadataRepository.createMetadata(opprettet, status, sendtInn = sendtInn)
        opprettSoknad(id = soknadId).also { soknadRepository.save(it) }

        return soknadId
    }
}
