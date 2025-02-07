package no.nav.sosialhjelp.soknad.api.informasjon

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.ControllerToNewDatamodellProxy
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class PabegynteSoknaderServiceTest {
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val soknadMetadataService: SoknadMetadataService = mockk()

    private val pabegynteSoknaderService =
        PabegynteSoknaderService(
            soknadMetadataService,
            soknadMetadataRepository,
        )

    @Test
    fun brukerHarIngenPabegynteSoknader() {
        every { soknadMetadataRepository.hentPabegynteSoknaderForBruker(any()) } returns emptyList()

        assertThat(pabegynteSoknaderService.hentPabegynteSoknaderForBruker("fnr")).isEmpty()

        ControllerToNewDatamodellProxy.nyDatamodellAktiv = false
    }

    @Test
    fun brukerHar1PabegyntSoknad() {
        val now = LocalDateTime.now()
        val soknadMetadata =
            SoknadMetadata(
                id = 0L,
                behandlingsId = "id",
                fnr = "fnr",
                opprettetDato = now,
                sistEndretDato = now,
                innsendtDato = now,
                kortSoknad = false,
            )
        every { soknadMetadataRepository.hentPabegynteSoknaderForBruker(any()) } returns listOf(soknadMetadata)

        val pabegyntSoknadList = pabegynteSoknaderService.hentPabegynteSoknaderForBruker("fnr")

        assertThat(pabegyntSoknadList).hasSize(1)
        assertThat(pabegyntSoknadList[0].behandlingsId).isEqualTo("id")
    }
}
