package no.nav.sosialhjelp.soknad.api.dittnav

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class DittNavMetadataServiceTest {

    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val dittNavMetadataService = DittNavMetadataService(soknadMetadataRepository)

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.environmentName } returns "p"
    }

    @AfterEach
    internal fun tearDown() {
        unmockkObject(MiljoUtils)
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

        val markert = dittNavMetadataService.oppdaterLestStatusForPabegyntSoknad("behandlingsId", "12345")
        assertThat(markert).isFalse
    }

    @Test
    fun markerPabegyntSoknadSomLest_skalGiFalse_hvisNoeFeiler() {
        val soknadMetadata = createSoknadMetadata(false)
        every { soknadMetadataRepository.hent(any()) } returns soknadMetadata
        every { soknadMetadataRepository.oppdaterLest(any(), any()) } throws RuntimeException("Noe feilet")

        val markert = dittNavMetadataService.oppdaterLestStatusForPabegyntSoknad("behandlingsId", "12345")
        assertThat(markert).isFalse
    }

    private fun createSoknadMetadata(lest: Boolean): SoknadMetadata {
        return SoknadMetadata(
            id = 0L,
            fnr = "12345",
            behandlingsId = "beh123",
            status = SoknadMetadataInnsendingStatus.UNDER_ARBEID,
            type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL,
            opprettetDato = LocalDateTime.now().minusDays(10),
            innsendtDato = LocalDateTime.now().minusDays(2),
            sistEndretDato = LocalDateTime.now().minusDays(2),
            lest = lest
        )
    }
}
