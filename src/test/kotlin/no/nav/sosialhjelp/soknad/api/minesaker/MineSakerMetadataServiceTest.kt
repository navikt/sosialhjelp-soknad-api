package no.nav.sosialhjelp.soknad.api.minesaker

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.domain.SoknadMetadata
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class MineSakerMetadataServiceTest {
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val mineSakerMetadataService = MineSakerMetadataService(soknadMetadataRepository)

    @Test
    fun skalHenteInnsendteSoknaderForBruker() {
        val soknadMetadata = SoknadMetadata()
        soknadMetadata.fnr = "12345"
        soknadMetadata.behandlingsId = "beh123"
        soknadMetadata.type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL
        soknadMetadata.innsendtDato = LocalDateTime.now()
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
