package no.nav.sosialhjelp.soknad.scheduled

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.OppgaveRepository
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.BatchSendtSoknadRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.BatchSoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus.UNDER_ARBEID
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataType
import no.nav.sosialhjelp.soknad.domain.FiksData
import no.nav.sosialhjelp.soknad.domain.FiksResultat
import no.nav.sosialhjelp.soknad.domain.Oppgave
import no.nav.sosialhjelp.soknad.domain.SendtSoknad
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

    private fun sendtSoknad(behandlingsId: String, eier: String, dagerSiden: Int): SendtSoknad {
        return SendtSoknad()
            .withBehandlingsId(behandlingsId)
            .withNavEnhetsnavn("")
            .withOrgnummer("")
            .withBrukerOpprettetDato(LocalDateTime.now().minusDays(dagerSiden.toLong()))
            .withBrukerFerdigDato(LocalDateTime.now().minusDays(dagerSiden.toLong()))
            .withSendtDato(LocalDateTime.now().minusDays(dagerSiden.toLong()))
            .withEier(eier)
            .withTilknyttetBehandlingsId("")
            .withFiksforsendelseId("")
            .withSendtSoknadId(1L)
    }

    private fun oppgave(behandlingsId: String, dagerSiden: Int): Oppgave {
        val oppgave = Oppgave()
        oppgave.behandlingsId = behandlingsId
        oppgave.status = Oppgave.Status.FERDIG
        oppgave.steg = 1
        oppgave.id = 1L
        oppgave.oppgaveData = FiksData()
        oppgave.nesteForsok = null
        oppgave.oppgaveResultat = FiksResultat()
        oppgave.type = ""
        oppgave.opprettet = LocalDateTime.now().minusDays(dagerSiden.toLong())
        oppgave.sistKjort = LocalDateTime.now().minusDays(dagerSiden.toLong())
        return oppgave
    }

    companion object {
        private const val EIER = "11111111111"
        private const val DAGER_GAMMEL_SOKNAD = 365
        private const val BEHANDLINGS_ID = "1100AAAAA"
    }
}
