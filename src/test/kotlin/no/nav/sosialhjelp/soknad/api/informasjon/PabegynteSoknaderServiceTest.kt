package no.nav.sosialhjelp.soknad.api.informasjon

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

internal class PabegynteSoknaderServiceTest {
    private val soknadMetadataService: SoknadMetadataService = mockk()

    private val pabegynteSoknaderService = PabegynteSoknaderService(soknadMetadataService)

    @Test
    fun brukerHarIngenPabegynteSoknader() {
        every { soknadMetadataService.getOpenSoknader(any()) } returns emptyList()

        assertThat(pabegynteSoknaderService.hentPabegynteSoknaderForBruker("fnr")).isEmpty()
    }

    @Test
    fun brukerHar1PabegyntSoknad() {
        val now = LocalDateTime.now()
        val soknadId = UUID.randomUUID()
        val soknadMetadata =
            no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata(
                soknadId = soknadId,
                personId = "fnr",
                tidspunkt =
                    Tidspunkt(
                        opprettet = now,
                        sistEndret = now,
                        sendtInn = now,
                    ),
                soknadType = SoknadType.STANDARD,
            )
        every { soknadMetadataService.getOpenSoknader(any()) } returns listOf(soknadMetadata)

        val pabegyntSoknadList = pabegynteSoknaderService.hentPabegynteSoknaderForBruker("fnr")

        assertThat(pabegyntSoknadList).hasSize(1)
        assertThat(pabegyntSoknadList[0].behandlingsId).isEqualTo(soknadId.toString())
    }
}
