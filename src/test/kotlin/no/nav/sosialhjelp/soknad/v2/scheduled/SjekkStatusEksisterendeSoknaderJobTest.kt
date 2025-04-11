package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.test.runTest
import no.nav.sosialhjelp.soknad.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.scheduled.jobs.SjekkStatusEksisterendeSoknaderJob
import no.nav.sosialhjelp.soknad.v2.scheduled.jobs.SoknaderFeilStatusException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.UUID

class SjekkStatusEksisterendeSoknaderJobTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var sjekkStatusSoknaderSendt: SjekkStatusEksisterendeSoknaderJob

    @BeforeEach
    fun setUp() {
        soknadMetadataRepository.deleteAll()
        soknadRepository.deleteAll()
    }

    @Test
    fun `Eksisterende soknader med status OPPRETTET kaster ikke feil`() =
        runTest {
            createMetadataAndSoknad(LocalDateTime.now().minusDays(10), SoknadStatus.OPPRETTET)
            assertThat(soknadRepository.findAll()).hasSize(1)

            assertDoesNotThrow { sjekkStatusSoknaderSendt.sjekkStatus() }
        }

    @Test
    fun `Eksisterende soknader som ikke har status OPPRETTET skal kaste feil`() =
        runTest {
            createMetadataAndSoknad(nowMinusDays(10), SoknadStatus.SENDT, nowMinusDays(10))
            createMetadataAndSoknad(nowMinusDays(10), SoknadStatus.MOTTATT_FSL, nowMinusDays(10))

            assertThrows<SoknaderFeilStatusException> { sjekkStatusSoknaderSendt.sjekkStatus() }
        }

    // TODO Inntil KS har byttet endepunkt til å sjekke SvarUt-status kan denne være disablet
    @Test
    @Disabled
    fun `Tar kun stilling til eksisterende soknader`() =
        runTest {
            createMetadataAndSoknad(LocalDateTime.now().minusDays(14), SoknadStatus.OPPRETTET)
            createMetadataAndSoknad(LocalDateTime.now().minusDays(14), SoknadStatus.OPPRETTET)
            val idFeilStatus = createMetadataAndSoknad(nowMinusDays(14), SoknadStatus.SENDT, nowMinusDays(10))

            assertThrows<SoknaderFeilStatusException> { sjekkStatusSoknaderSendt.sjekkStatus() }

            soknadRepository.deleteById(idFeilStatus)

            SoknadMetadata(
                UUID.randomUUID(),
                "12345612345",
                SoknadStatus.SENDT,
                Tidspunkt(sendtInn = nowWithMillis().minusDays(4)),
            ).also { soknadMetadataRepository.save(it) }

            soknadMetadataRepository.findAll().also { metadatas ->
                assertThat(metadatas)
                    .hasSize(4)
                    .anyMatch { it.status == SoknadStatus.SENDT }
                    .anyMatch { it.status == SoknadStatus.OPPRETTET }
            }
            assertDoesNotThrow { sjekkStatusSoknaderSendt.sjekkStatus() }
        }

    private fun nowMinusDays(days: Long): LocalDateTime = LocalDateTime.now().minusDays(days)

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
