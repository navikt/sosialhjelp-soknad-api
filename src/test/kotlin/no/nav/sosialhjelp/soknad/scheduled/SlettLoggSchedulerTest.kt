package no.nav.sosialhjelp.soknad.scheduled

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.Oppgave
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.OppgaveRepository
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.Status
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.BatchSendtSoknadRepository
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknad
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.BatchSoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.domain.SoknadMetadata
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.UNDER_ARBEID
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataType
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Optional

internal class SlettLoggSchedulerTest {
    private val leaderElection: LeaderElection = mockk()
    private val batchSendtSoknadRepository: BatchSendtSoknadRepository = mockk()
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val batchSoknadMetadataRepository: BatchSoknadMetadataRepository = mockk()
    private val oppgaveRepository: OppgaveRepository = mockk()

    private val scheduler = SlettLoggScheduler(
        leaderElection,
        batchSoknadMetadataRepository,
        batchSendtSoknadRepository,
        oppgaveRepository,
        batchEnabled = true,
        schedulerDisabled = false
    )

    @BeforeEach
    fun setUp() {
        every { leaderElection.isLeader() } returns true
        every { soknadMetadataRepository.hentNesteId() } returns 123L
    }

    @Test
    fun skalSletteForeldetLoggFraDatabase() {
        val oppgave = oppgave(BEHANDLINGS_ID, DAGER_GAMMEL_SOKNAD + 1)
        val sendtSoknad = sendtSoknad(BEHANDLINGS_ID, EIER, DAGER_GAMMEL_SOKNAD + 1)
        val soknadMetadata = soknadMetadata(BEHANDLINGS_ID, UNDER_ARBEID, DAGER_GAMMEL_SOKNAD + 1)

        every {
            batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMEL_SOKNAD)
        } returns Optional.of(soknadMetadata) andThen Optional.empty()

        every {
            oppgaveRepository.hentOppgave(BEHANDLINGS_ID)
        } returns Optional.of(oppgave)

        every {
            batchSendtSoknadRepository.hentSendtSoknad(BEHANDLINGS_ID)
        } returns Optional.of(sendtSoknad.sendtSoknadId)

        every { oppgaveRepository.slettOppgave(any()) } just runs
        every { batchSendtSoknadRepository.slettSendtSoknad(any()) } just runs
        every { batchSoknadMetadataRepository.slettSoknadMetaData(any()) } just runs

        scheduler.slettLogger()

        verify { oppgaveRepository.slettOppgave(BEHANDLINGS_ID) }
        verify { batchSendtSoknadRepository.slettSendtSoknad(sendtSoknad.sendtSoknadId) }
        verify { batchSoknadMetadataRepository.slettSoknadMetaData(BEHANDLINGS_ID) }
    }

    @Test
    fun skalSletteForeldetLoggFraDatabaseSelvOmIkkeAlleTabelleneInneholderBehandlingsIdeen() {
        val oppgave = oppgave(BEHANDLINGS_ID, DAGER_GAMMEL_SOKNAD + 1)
        val sendtSoknad = sendtSoknad(BEHANDLINGS_ID, EIER, DAGER_GAMMEL_SOKNAD + 1)
        val soknadMetadata = soknadMetadata(BEHANDLINGS_ID, UNDER_ARBEID, DAGER_GAMMEL_SOKNAD + 1)

        every {
            batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMEL_SOKNAD)
        } returns Optional.of(soknadMetadata) andThen Optional.empty()

        every {
            oppgaveRepository.hentOppgave(BEHANDLINGS_ID)
        } returns Optional.of(oppgave)

        every {
            batchSendtSoknadRepository.hentSendtSoknad(BEHANDLINGS_ID)
        } returns Optional.empty()

        every { oppgaveRepository.slettOppgave(any()) } just runs

        scheduler.slettLogger()

        verify(exactly = 1) { oppgaveRepository.slettOppgave(BEHANDLINGS_ID) }
        verify(exactly = 0) { batchSendtSoknadRepository.slettSendtSoknad(sendtSoknad.sendtSoknadId) }
        verify(exactly = 1) { batchSoknadMetadataRepository.slettSoknadMetaData(BEHANDLINGS_ID) }
    }

    @Test
    fun skalIkkeSletteLoggSomErUnderEttAarGammelt() {
        every { batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMEL_SOKNAD) } returns Optional.empty()

        scheduler.slettLogger()

        verify(exactly = 0) { oppgaveRepository.slettOppgave(any()) }
        verify(exactly = 0) { batchSendtSoknadRepository.slettSendtSoknad(any()) }
        verify(exactly = 0) { batchSoknadMetadataRepository.slettSoknadMetaData(any()) }
    }

    private fun soknadMetadata(
        behandlingsId: String,
        status: SoknadMetadataInnsendingStatus,
        dagerSiden: Int
    ): SoknadMetadata {
        val meta = SoknadMetadata()
        meta.id = soknadMetadataRepository.hentNesteId()
        meta.behandlingsId = behandlingsId
        meta.fnr = EIER
        meta.type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL
        meta.skjema = ""
        meta.status = status
        meta.innsendtDato = LocalDateTime.now().minusDays(dagerSiden.toLong())
        meta.opprettetDato = LocalDateTime.now().minusDays(dagerSiden.toLong())
        meta.sistEndretDato = LocalDateTime.now().minusDays(dagerSiden.toLong())
        return meta
    }

    private fun sendtSoknad(behandlingsId: String, eier: String, dagerSiden: Int): SendtSoknad {
        return SendtSoknad(
            sendtSoknadId = 1L,
            behandlingsId = behandlingsId,
            tilknyttetBehandlingsId = "",
            eier = eier,
            fiksforsendelseId = "",
            orgnummer = "",
            navEnhetsnavn = "",
            brukerOpprettetDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            brukerFerdigDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            sendtDato = LocalDateTime.now().minusDays(dagerSiden.toLong())
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
            retries = 0
        )
    }

    companion object {
        private const val EIER = "11111111111"
        private const val DAGER_GAMMEL_SOKNAD = 365
        private const val BEHANDLINGS_ID = "1100AAAAA"
    }
}
