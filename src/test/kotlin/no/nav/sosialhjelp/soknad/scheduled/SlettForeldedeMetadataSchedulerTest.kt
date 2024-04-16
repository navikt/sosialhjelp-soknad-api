package no.nav.sosialhjelp.soknad.scheduled

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.BatchSoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus.UNDER_ARBEID
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataType
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class SlettForeldedeMetadataSchedulerTest {
    private val leaderElection: LeaderElection = mockk()
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val batchSoknadMetadataRepository: BatchSoknadMetadataRepository = mockk()

    private val scheduler = SlettForeldedeMetadataScheduler(
        leaderElection,
        batchSoknadMetadataRepository,
        batchEnabled = true,
        schedulerDisabled = false
    )

    @BeforeEach
    fun setUp() {
        every { leaderElection.isLeader() } returns true
        every { soknadMetadataRepository.hentNesteId() } returns 123L
    }

    @Test
    fun skalSletteForeldedeMetadataFraDatabase() {
        val soknadMetadata = soknadMetadata(BEHANDLINGS_ID, UNDER_ARBEID, DAGER_GAMMEL_SOKNAD + 1)

        every {
            batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMEL_SOKNAD)
        } returns listOf(soknadMetadata) andThen emptyList()

        every { batchSoknadMetadataRepository.slettSoknadMetaDataer(any()) } just runs

        scheduler.slettForeldedeMetadata()

        verify { batchSoknadMetadataRepository.slettSoknadMetaDataer(listOf(BEHANDLINGS_ID)) }
    }

    @Test
    fun skalSletteForeldedeMetadataFraDatabaseSelvOmIkkeAlleTabelleneInneholderBehandlingsIdeen() {
        val soknadMetadata = soknadMetadata(BEHANDLINGS_ID, UNDER_ARBEID, DAGER_GAMMEL_SOKNAD + 1)

        every {
            batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMEL_SOKNAD)
        } returns listOf(soknadMetadata) andThen emptyList()

        scheduler.slettForeldedeMetadata()

        verify(exactly = 1) { batchSoknadMetadataRepository.slettSoknadMetaDataer(listOf(BEHANDLINGS_ID)) }
    }

    @Test
    fun skalIkkeSletteMetadataSomErUnderEttAarGammelt() {
        every { batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMEL_SOKNAD) } returns emptyList()

        scheduler.slettForeldedeMetadata()

        verify(exactly = 0) { batchSoknadMetadataRepository.slettSoknadMetaDataer(any()) }
    }

    private fun soknadMetadata(
        behandlingsId: String,
        status: SoknadMetadataInnsendingStatus,
        dagerSiden: Int
    ): SoknadMetadata {
        return SoknadMetadata(
            id = soknadMetadataRepository.hentNesteId(),
            behandlingsId = behandlingsId,
            fnr = EIER,
            type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL,
            skjema = "",
            status = status,
            innsendtDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            opprettetDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            sistEndretDato = LocalDateTime.now().minusDays(dagerSiden.toLong())
        )
    }

    companion object {
        private const val EIER = "11111111111"
        private const val DAGER_GAMMEL_SOKNAD = 365
        private const val BEHANDLINGS_ID = "1100AAAAA"
    }
}
