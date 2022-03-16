package no.nav.sosialhjelp.soknad.api.innsyn

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.sosialhjelp.soknad.api.innsyn.SoknadOversiktService.Companion.DEFAULT_TITTEL
import no.nav.sosialhjelp.soknad.api.innsyn.SoknadOversiktService.Companion.KILDE_SOKNAD_API
import no.nav.sosialhjelp.soknad.api.innsyn.dto.SoknadOversiktDto
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Timestamp
import java.time.LocalDateTime

internal class SoknadOversiktServiceTest {
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val service = SoknadOversiktService(soknadMetadataRepository)

    private lateinit var soknadMetadata: SoknadMetadata

    @BeforeEach
    fun setUp() {
        soknadMetadata = SoknadMetadata(
            id = 0L,
            behandlingsId = "beh123",
            fnr = "12345",
            type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL,
            status = SoknadMetadataInnsendingStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.of(2018, 4, 11, 13, 30, 0),
            sistEndretDato = LocalDateTime.of(2018, 4, 11, 13, 30, 0),
            innsendtDato = LocalDateTime.of(2018, 4, 11, 13, 30, 0)
        )

        mockkObject(MiljoUtils)
        every { MiljoUtils.environmentName } returns "p"
    }

    @AfterEach
    internal fun tearDown() {
        unmockkObject(MiljoUtils)
    }

    @Test
    fun hentAlleSoknaderForBruker() {
        every { soknadMetadataRepository.hentSvarUtInnsendteSoknaderForBruker("12345") } returns listOf(soknadMetadata)
        val resultat: List<SoknadOversiktDto> = service.hentSvarUtSoknaderFor("12345")
        assertThat(resultat).hasSize(1)

        val soknad = resultat[0]
        assertThat(soknad.fiksDigisosId).isNull()
        assertThat(soknad.soknadTittel).contains(DEFAULT_TITTEL).contains(soknadMetadata.behandlingsId)
        assertThat(soknad.status).isEqualTo(SoknadMetadataInnsendingStatus.UNDER_ARBEID.toString())
        assertThat(soknad.sistOppdatert).isEqualTo(Timestamp.valueOf(soknadMetadata.innsendtDato))
        assertThat(soknad.antallNyeOppgaver).isNull()
        assertThat(soknad.kilde).isEqualTo(KILDE_SOKNAD_API)
        assertThat(soknad.url).contains(soknadMetadata.behandlingsId)
    }
}
