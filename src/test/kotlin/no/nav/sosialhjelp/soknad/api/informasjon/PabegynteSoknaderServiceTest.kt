package no.nav.sosialhjelp.soknad.api.informasjon

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class PabegynteSoknaderServiceTest {

    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val pabegynteSoknaderService = PabegynteSoknaderService(soknadMetadataRepository)

    @Test
    fun brukerHarIngenPabegynteSoknader() {
        every { soknadMetadataRepository.hentPabegynteSoknaderForBruker(any()) } returns emptyList()

        assertThat(pabegynteSoknaderService.hentPabegynteSoknaderForBruker("fnr")).isEmpty()
    }

    @Test
    fun brukerHar1PabegyntSoknad() {
        val now = LocalDateTime.now()
        val soknadMetadata = SoknadMetadata(
            id = 0L,
            behandlingsId = "id",
            fnr = "fnr",
            opprettetDato = now,
            sistEndretDato = now,
            innsendtDato = now
        )
        every { soknadMetadataRepository.hentPabegynteSoknaderForBruker(any()) } returns listOf(soknadMetadata)

        val pabegyntSoknadList = pabegynteSoknaderService.hentPabegynteSoknaderForBruker("fnr")

        assertThat(pabegyntSoknadList).hasSize(1)
        assertThat(pabegyntSoknadList[0].getSistOppdatert()).isEqualTo(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        assertThat(pabegyntSoknadList[0].behandlingsId).isEqualTo("id")
    }
}
