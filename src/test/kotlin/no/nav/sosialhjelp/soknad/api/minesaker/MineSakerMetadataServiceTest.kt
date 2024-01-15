package no.nav.sosialhjelp.soknad.api.minesaker

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class MineSakerMetadataServiceTest {
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val mineSakerMetadataService = MineSakerMetadataService(soknadMetadataRepository)

    @Test
    fun skalHenteInnsendteSoknaderForBruker() {
        val soknadMetadata = SoknadMetadata(
            id = 0L,
            behandlingsId = "beh123",
            fnr = "12345",
            type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now(),
            innsendtDato = LocalDateTime.now(),
        )

        every { soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker("12345") } returns listOf(soknadMetadata)

        val dtos = mineSakerMetadataService.hentInnsendteSoknader("12345")
        assertThat(dtos).hasSize(1)
    }

    @Test
    fun skalReturnereTomListeVedTomListe() {
        every { soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker("12345") } returns emptyList()

        val dtos = mineSakerMetadataService.hentInnsendteSoknader("12345")
        assertThat(dtos).isEmpty()
    }
}
