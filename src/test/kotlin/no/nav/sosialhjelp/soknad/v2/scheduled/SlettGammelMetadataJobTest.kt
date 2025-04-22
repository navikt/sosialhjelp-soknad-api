package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.test.runTest
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import no.nav.sosialhjelp.soknad.v2.scheduled.jobs.SlettGammelMetadataJob
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

class SlettGammelMetadataJobTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var slettGammelMetadataJob: SlettGammelMetadataJob

    @BeforeEach
    fun setup() {
        soknadMetadataRepository.deleteAll()
    }

    @Test
    fun `planlagt jobb skal slette soknader eldre enn 200 dager`() =
        runTest(timeout = 5.seconds) {
            soknadMetadataRepository.save(opprettSoknadMetadata(opprettetDato = LocalDateTime.now().minusDays(201)))

            slettGammelMetadataJob.slettGammelMetadata()

            assertThat(soknadMetadataRepository.findAll()).isEmpty()
        }

    @Test
    fun `Planlagt jobb skal ikke slette soknader nyere enn 200 dager`() =
        runTest(timeout = 5.seconds) {
            soknadMetadataRepository.save(opprettSoknadMetadata(opprettetDato = LocalDateTime.now().minusDays(199)))

            slettGammelMetadataJob.slettGammelMetadata()

            assertThat(soknadMetadataRepository.findAll()).isNotEmpty()
        }
}
