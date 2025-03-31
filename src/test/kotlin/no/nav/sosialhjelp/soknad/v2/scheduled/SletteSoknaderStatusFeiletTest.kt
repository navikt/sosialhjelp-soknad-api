package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.test.runTest
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.INNSENDING_FEILET
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.MOTTATT_FSL
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.OPPRETTET
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus.SENDT
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class SletteSoknaderStatusFeiletTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var sletteJob: SletteSoknaderStatusFeiletJob

    @BeforeEach
    fun setUp() {
        soknadRepository.deleteAll()
        soknadMetadataRepository.deleteAll()
    }

    @Test
    fun `Soknader som feilet ved innsending skal slettes etter 5 ekstra dager`() =
        runTest(timeout = 5.seconds) {
            createMetadataAndSoknad(nowMinusDays(20L), INNSENDING_FEILET)

            sletteJob.sletteSoknaderStatusFeilet()

            soknadRepository.findAll().also { assertThat(it).isEmpty() }
            soknadMetadataRepository.findAll()
                .also { metadata -> assertThat(metadata).hasSize(1).allMatch { it.status == INNSENDING_FEILET } }
        }

    @Test
    fun `Soknad med status feilet yngre enn 14 + 5 dager skal ikke slettes`() =
        runTest(timeout = 5.seconds) {
            createMetadataAndSoknad(nowMinusDays(14), INNSENDING_FEILET)

            sletteJob.sletteSoknaderStatusFeilet()

            soknadRepository.findAll().also { assertThat(it).hasSize(1) }
            soknadMetadataRepository.findAll()
                .also { metadata -> assertThat(metadata).hasSize(1).allMatch { it.status == INNSENDING_FEILET } }
        }

    @Test
    fun `Slette-jobb skal kun ta stilling til INNSENDING_FEILET`() =
        runTest(timeout = 5.seconds) {
            createMetadataAndSoknad(nowMinusDays(20), OPPRETTET)
            createMetadataAndSoknad(nowMinusDays(20), SENDT)
            createMetadataAndSoknad(nowMinusDays(20), MOTTATT_FSL)
            createMetadataAndSoknad(nowMinusDays(20), INNSENDING_FEILET)

            sletteJob.sletteSoknaderStatusFeilet()

            soknadRepository.findAll().also { assertThat(it).hasSize(2) }
            soknadMetadataRepository.findAll().also { metadatas ->
                assertThat(metadatas)
                    .isNotEmpty()
                    .noneMatch { it.status == INNSENDING_FEILET }
            }
        }

    private fun createMetadataAndSoknad(
        opprettet: LocalDateTime,
        status: SoknadStatus,
        sendtInn: LocalDateTime = LocalDateTime.now(),
    ): UUID {
        val soknadId = soknadMetadataRepository.createMetadata(opprettet, status, sendtInn = sendtInn)
        opprettSoknad(id = soknadId).also { soknadRepository.save(it) }

        return soknadId
    }
}
