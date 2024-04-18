package no.nav.sosialhjelp.soknad.innsending.svarut

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.Oppgave
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.OppgaveRepository
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.Status
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class OppgaveHandtererImplTest {
    private val fiksHandterer: FiksHandterer = mockk()
    private val oppgaveRepository: OppgaveRepository = mockk()
    private val prometheusMetricsService: PrometheusMetricsService = mockk(relaxed = true)
    private val leaderElection: LeaderElection = mockk()

    private val oppgaveHandterer =
        OppgaveHandtererImpl(
            fiksHandterer,
            oppgaveRepository,
            schedulerDisabled = false,
            prometheusMetricsService,
            leaderElection,
        )

    private val oppgaveSlot = slot<Oppgave>()

    @BeforeEach
    internal fun setUp() {
        every { leaderElection.isLeader() } returns true
    }

    @Test
    fun prosessereFeilendeOppgaveSkalSetteNesteForsok() {
        val oppgave =
            Oppgave(
                id = 0L,
                behandlingsId = "behandlingsId",
                type = FiksHandterer.FIKS_OPPGAVE,
                status = Status.UNDER_ARBEID,
                steg = 21,
                opprettet = LocalDateTime.now(),
                sistKjort = null,
                nesteForsok = LocalDateTime.now(),
                retries = 0,
            )

        every { oppgaveRepository.hentNeste() } returns oppgave andThen null
        every { fiksHandterer.eksekver(oppgave) } throws IllegalStateException()
        every { oppgaveRepository.oppdater(capture(oppgaveSlot)) } just runs

        oppgaveHandterer.prosesserOppgaver()

        verify(exactly = 1) { oppgaveRepository.oppdater(oppgaveSlot.captured) }
        assertThat(oppgaveSlot.captured.status).isEqualTo(Status.KLAR)
        assertThat(oppgaveSlot.captured.nesteForsok).isNotNull
    }

    @Test
    fun `rapporterFeilede skal rapportere til prometheus`() {
        every { oppgaveRepository.hentAntallFeilede() } returns 1
        every { oppgaveRepository.hentAntallStuckUnderArbeid() } returns 2

        oppgaveHandterer.rapporterFeilede()

        verify(exactly = 1) { prometheusMetricsService.resetOppgaverFeiletOgStuckUnderArbeid() }
        verify(exactly = 1) { prometheusMetricsService.reportOppgaverFeilet(1) }
        verify(exactly = 1) { prometheusMetricsService.reportOppgaverStuckUnderArbeid(2) }
    }
}
