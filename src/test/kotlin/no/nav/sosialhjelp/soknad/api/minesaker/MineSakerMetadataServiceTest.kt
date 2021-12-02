package no.nav.sosialhjelp.soknad.api.minesaker

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType
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
        soknadMetadata.type = SoknadType.SEND_SOKNAD_KOMMUNAL
        soknadMetadata.innsendtDato = LocalDateTime.now()
        every { soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker("12345") } returns listOf(soknadMetadata)

        val dtos = mineSakerMetadataService.hentInnsendteSoknader("12345")
        assertThat(dtos).hasSize(1)
    }

    @Test
    fun skalReturnereTomListeVedNull() {
        every { soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker("12345") } returns null

        val dtos = mineSakerMetadataService.hentInnsendteSoknader("12345")
        assertThat(dtos).isEmpty()
    }

    @Test
    fun skalReturnereTomListeVedTomListe() {
        every { soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker("12345") } returns emptyList()

        val dtos = mineSakerMetadataService.hentInnsendteSoknader("12345")
        assertThat(dtos).isEmpty()
    }
}
