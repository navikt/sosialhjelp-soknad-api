package no.nav.sosialhjelp.soknad.v2.scheduled

import no.nav.sosialhjelp.soknad.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
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
    fun `Eksisterende soknader med status OPPRETTET kaster ikke feil`() {
        createMetadataAndSoknad(LocalDateTime.now().minusDays(10), SoknadStatus.OPPRETTET)
        assertThat(soknadRepository.findAll()).hasSize(1)

        assertDoesNotThrow { sjekkStatusSoknaderSendt.sjekkStatus() }
    }

    @Test
    fun `Eksisterende soknader som ikke har status OPPRETTET skal kaste feil`() {
        createMetadataAndSoknad(nowMinusDays(10), SoknadStatus.SENDT, nowMinusDays(10))
        createMetadataAndSoknad(nowMinusDays(10), SoknadStatus.MOTTATT_FSL, nowMinusDays(10))

        assertThatThrownBy { sjekkStatusSoknaderSendt.sjekkStatus() }
            .isInstanceOf(SoknaderFeilStatusException::class.java)
    }

    @Test
    fun `Tar kun stilling til eksisterende soknader`() {
        createMetadataAndSoknad(LocalDateTime.now().minusDays(14), SoknadStatus.OPPRETTET)
        createMetadataAndSoknad(LocalDateTime.now().minusDays(14), SoknadStatus.OPPRETTET)
        val idFeilStatus = createMetadataAndSoknad(nowMinusDays(14), SoknadStatus.SENDT, nowMinusDays(10))

        assertThatThrownBy { sjekkStatusSoknaderSendt.sjekkStatus() }
            .isInstanceOf(SoknaderFeilStatusException::class.java)

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

    @Test
    fun `Eksisterende soknader sendt inn for under en uke siden skal ikke kaste feil`() {
        createMetadataAndSoknad(LocalDateTime.now().minusDays(14), SoknadStatus.OPPRETTET)
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
