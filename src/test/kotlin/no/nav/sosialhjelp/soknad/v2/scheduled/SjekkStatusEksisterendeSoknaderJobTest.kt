package no.nav.sosialhjelp.soknad.v2.scheduled

import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
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
        createMetadataAndSoknad(LocalDateTime.now().minusDays(10), SoknadStatus.SENDT)
        createMetadataAndSoknad(LocalDateTime.now().minusDays(10), SoknadStatus.MOTTATT_FSL)

        assertThatThrownBy { sjekkStatusSoknaderSendt.sjekkStatus() }
            .isInstanceOf(EksisterendeSoknaderStatusException::class.java)
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
