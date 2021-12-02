package no.nav.sosialhjelp.soknad.api.dittnav

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class DittNavMetadataServiceTest {

    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val dittNavMetadataService = DittNavMetadataService(soknadMetadataRepository)

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
    }

    @Test
    fun skalHenteAktivePabegynteSoknaderForBruker() {
        val soknadMetadata = createSoknadMetadata(false)
        every { soknadMetadataRepository.hentPabegynteSoknaderForBruker("12345", false) } returns listOf(soknadMetadata)

        val dtos = dittNavMetadataService.hentAktivePabegynteSoknader("12345")
        assertThat(dtos).hasSize(1)
        assertThat(dtos[0].eventId).isEqualTo(soknadMetadata.behandlingsId + "_aktiv")
        assertThat(dtos[0].grupperingsId).isEqualTo(soknadMetadata.behandlingsId)
        assertThat(dtos[0].isAktiv).isTrue
    }

    @Test
    fun skalHenteInaktivePabegynteSoknaderForBruker() {
        val soknadMetadata = createSoknadMetadata(true)
        every { soknadMetadataRepository.hentPabegynteSoknaderForBruker("12345", true) } returns listOf(soknadMetadata)

        val dtos = dittNavMetadataService.hentInaktivePabegynteSoknader("12345")
        assertThat(dtos).hasSize(1)
        assertThat(dtos[0].eventId).isEqualTo(soknadMetadata.behandlingsId + "_inaktiv")
        assertThat(dtos[0].grupperingsId).isEqualTo(soknadMetadata.behandlingsId)
        assertThat(dtos[0].isAktiv).isFalse
    }

    @Test
    fun markerPabegyntSoknadSomLest_skalGiFalse_hvisRepositoryReturnererNull() {
        every { soknadMetadataRepository.hent(any()) } returns null

        val markert = dittNavMetadataService.oppdaterLestDittNavForPabegyntSoknad("behandlingsId", "12345")
        assertThat(markert).isFalse
    }

    @Test
    fun markerPabegyntSoknadSomLest_skalGiFalse_hvisNoeFeiler() {
        val soknadMetadata = createSoknadMetadata(false)
        every { soknadMetadataRepository.hent(any()) } returns soknadMetadata
        every { soknadMetadataRepository.oppdaterLestDittNav(any(), any()) } throws RuntimeException("Noe feilet")

        val markert = dittNavMetadataService.oppdaterLestDittNavForPabegyntSoknad("behandlingsId", "12345")
        assertThat(markert).isFalse
    }

    private fun createSoknadMetadata(lestDittNav: Boolean): SoknadMetadata {
        val soknadMetadata = SoknadMetadata()
        soknadMetadata.fnr = "12345"
        soknadMetadata.behandlingsId = "beh123"
        soknadMetadata.status = SoknadMetadataInnsendingStatus.UNDER_ARBEID
        soknadMetadata.type = SoknadType.SEND_SOKNAD_KOMMUNAL
        soknadMetadata.opprettetDato = LocalDateTime.now().minusDays(10)
        soknadMetadata.innsendtDato = LocalDateTime.now().minusDays(2)
        soknadMetadata.sistEndretDato = LocalDateTime.now().minusDays(2)
        soknadMetadata.lestDittNav = lestDittNav
        return soknadMetadata
    }
}
