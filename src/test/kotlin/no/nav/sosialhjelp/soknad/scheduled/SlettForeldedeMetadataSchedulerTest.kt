package no.nav.sosialhjelp.soknad.scheduled

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.Oppgave
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.OppgaveRepository
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.Status
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
    private val oppgaveRepository: OppgaveRepository = mockk()

    private val scheduler = SlettForeldedeMetadataScheduler(
        leaderElection,
        batchSoknadMetadataRepository,
        oppgaveRepository,
        batchEnabled = true,
        schedulerDisabled = false,
    )

    @BeforeEach
    fun setUp() {
        every { leaderElection.isLeader() } returns true
        every { soknadMetadataRepository.hentNesteId() } returns 123L
    }

    @Test
    fun skalSletteForeldedeMetadataFraDatabase() {
        val oppgave = oppgave(BEHANDLINGS_ID, DAGER_GAMMEL_SOKNAD + 1)
        val soknadMetadata = soknadMetadata(BEHANDLINGS_ID, UNDER_ARBEID, DAGER_GAMMEL_SOKNAD + 1)

        every {
            batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMEL_SOKNAD)
        } returns listOf(soknadMetadata) andThen emptyList()

        every { oppgaveRepository.hentOppgaveIdList(listOf(BEHANDLINGS_ID)) } returns listOf(oppgave.id)

        every { oppgaveRepository.slettOppgaver(any()) } just runs
        every { batchSoknadMetadataRepository.slettSoknadMetaDataer(any()) } just runs

        scheduler.slettForeldedeMetadata()

        verify { oppgaveRepository.slettOppgaver(listOf(oppgave.id)) }
        verify { batchSoknadMetadataRepository.slettSoknadMetaDataer(listOf(BEHANDLINGS_ID)) }
    }

    @Test
    fun skalSletteForeldedeMetadataFraDatabaseSelvOmIkkeAlleTabelleneInneholderBehandlingsIdeen() {
        val oppgave = oppgave(BEHANDLINGS_ID, DAGER_GAMMEL_SOKNAD + 1)
        val soknadMetadata = soknadMetadata(BEHANDLINGS_ID, UNDER_ARBEID, DAGER_GAMMEL_SOKNAD + 1)

        every {
            batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMEL_SOKNAD)
        } returns listOf(soknadMetadata) andThen emptyList()

        every { oppgaveRepository.hentOppgaveIdList(listOf(BEHANDLINGS_ID)) } returns listOf(oppgave.id)

        every { oppgaveRepository.slettOppgaver(any()) } just runs

        scheduler.slettForeldedeMetadata()

        verify(exactly = 1) { oppgaveRepository.slettOppgaver(listOf(oppgave.id)) }
        verify(exactly = 1) { batchSoknadMetadataRepository.slettSoknadMetaDataer(listOf(BEHANDLINGS_ID)) }
    }

    @Test
    fun skalIkkeSletteMetadataSomErUnderEttAarGammelt() {
        every { batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMEL_SOKNAD) } returns emptyList()

        scheduler.slettForeldedeMetadata()

        verify(exactly = 0) { oppgaveRepository.slettOppgaver(any()) }
        verify(exactly = 0) { batchSoknadMetadataRepository.slettSoknadMetaDataer(any()) }
    }

    private fun soknadMetadata(
        behandlingsId: String,
        status: SoknadMetadataInnsendingStatus,
        dagerSiden: Int,
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
            sistEndretDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
        )
    }

    private fun oppgave(behandlingsId: String, dagerSiden: Int): Oppgave {
        return Oppgave(
            id = 1L,
            behandlingsId = behandlingsId,
            type = "",
            status = Status.FERDIG,
            steg = 1,
            opprettet = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            sistKjort = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            nesteForsok = null,
            retries = 0,
        )
    }

    companion object {
        private const val EIER = "11111111111"
        private const val DAGER_GAMMEL_SOKNAD = 365
        private const val BEHANDLINGS_ID = "1100AAAAA"
    }
}
