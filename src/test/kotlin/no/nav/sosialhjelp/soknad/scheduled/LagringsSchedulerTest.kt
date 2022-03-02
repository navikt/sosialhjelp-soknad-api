package no.nav.sosialhjelp.soknad.scheduled

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.HenvendelseService
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class LagringsSchedulerTest {
    private val leaderElection: LeaderElection = mockk()
    private val henvendelseService: HenvendelseService = mockk()
    private val batchSoknadUnderArbeidRepository: BatchSoknadUnderArbeidRepository = mockk()

    private val scheduler = LagringsScheduler(
        leaderElection,
        henvendelseService,
        batchSoknadUnderArbeidRepository,
        batchEnabled = true,
        schedulerDisabled = false
    )

    @BeforeEach
    fun setup() {
        every { leaderElection.isLeader() } returns true
    }

    @Test
    fun skalAvbryteIHenvendelseOgSletteFraDatabase() {
        val behandlingsId = "2"
        val tilknyttetBehandlingsId = "1"
        val soknadId: Long = 2
        val soknadUnderArbeid = SoknadUnderArbeid()
            .withSoknadId(soknadId)
            .withEier("11111111111")
            .withBehandlingsId(behandlingsId)
            .withStatus(SoknadUnderArbeidStatus.UNDER_ARBEID)
            .withTilknyttetBehandlingsId(tilknyttetBehandlingsId)

        every { batchSoknadUnderArbeidRepository.hentForeldedeEttersendelser() } returns listOf(soknadUnderArbeid)
        every { henvendelseService.avbrytSoknad(any(), any()) } just runs
        every { batchSoknadUnderArbeidRepository.slettSoknad(any()) } just runs

        scheduler.slettForeldedeEttersendelserFraSoknadUnderArbeidDatabase()

        verify { henvendelseService.avbrytSoknad(behandlingsId, true) }
        verify { batchSoknadUnderArbeidRepository.slettSoknad(any()) }
    }

    @Test
    fun skalIkkeAvbryteIHenvendelseOgSletteFraDatabaseDersomDetIkkeErEttersendelse() {
        val behandlingsId = "2"
        val soknadId: Long = 2
        val soknadUnderArbeid = SoknadUnderArbeid()
            .withSoknadId(soknadId)
            .withEier("11111111111")
            .withBehandlingsId(behandlingsId)
            .withStatus(SoknadUnderArbeidStatus.UNDER_ARBEID)

        every { batchSoknadUnderArbeidRepository.hentForeldedeEttersendelser() } returns listOf(soknadUnderArbeid)

        scheduler.slettForeldedeEttersendelserFraSoknadUnderArbeidDatabase()

        verify(exactly = 0) { henvendelseService.avbrytSoknad(behandlingsId, true) }
        verify(exactly = 0) { batchSoknadUnderArbeidRepository.slettSoknad(any()) }
    }
}
