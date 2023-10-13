package no.nav.sosialhjelp.soknad.scheduled

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.BatchSoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.OldSoknadService
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class SlettForeldedeEttersendelserSchedulerTest {
    private val leaderElection: LeaderElection = mockk()
    private val oldSoknadService: OldSoknadService = mockk()
    private val batchSoknadUnderArbeidRepository: BatchSoknadUnderArbeidRepository = mockk()

    private val scheduler = SlettForeldedeEttersendelserScheduler(
        leaderElection,
        oldSoknadService,
        batchSoknadUnderArbeidRepository,
        batchEnabled = true,
        schedulerDisabled = false
    )

    @BeforeEach
    fun setup() {
        every { leaderElection.isLeader() } returns true
    }

    @Test
    fun skalAvbryteForeldedeEttersendelserOgSletteFraDatabase() {
        val behandlingsId = "2"
        val tilknyttetBehandlingsId = "1"
        val soknadId: Long = 2
        val soknadUnderArbeid = SoknadUnderArbeid(
            soknadId = soknadId,
            versjon = 1L,
            behandlingsId = behandlingsId,
            tilknyttetBehandlingsId = tilknyttetBehandlingsId,
            eier = "11111111111",
            jsonInternalSoknad = null,
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )

        every { batchSoknadUnderArbeidRepository.hentForeldedeEttersendelser() } returns listOf(soknadUnderArbeid)
        every { oldSoknadService.settSoknadMetadataAvbrutt(any(), any()) } just runs
        every { batchSoknadUnderArbeidRepository.slettSoknad(any()) } just runs

        scheduler.slettForeldedeEttersendelser()

        verify { oldSoknadService.settSoknadMetadataAvbrutt(behandlingsId, true) }
        verify { batchSoknadUnderArbeidRepository.slettSoknad(any()) }
    }

    @Test
    fun skalIkkeAvbryteSoknadUnderArbeidOgSletteFraDatabaseDersomDetIkkeErEttersendelse() {
        val behandlingsId = "2"
        val soknadId: Long = 2
        val soknadUnderArbeid = SoknadUnderArbeid(
            soknadId = soknadId,
            versjon = 1L,
            behandlingsId = behandlingsId,
            tilknyttetBehandlingsId = null,
            eier = "11111111111",
            jsonInternalSoknad = null,
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )

        every { batchSoknadUnderArbeidRepository.hentForeldedeEttersendelser() } returns listOf(soknadUnderArbeid)

        scheduler.slettForeldedeEttersendelser()

        verify(exactly = 0) { oldSoknadService.settSoknadMetadataAvbrutt(behandlingsId, true) }
        verify(exactly = 0) { batchSoknadUnderArbeidRepository.slettSoknad(any()) }
    }
}
