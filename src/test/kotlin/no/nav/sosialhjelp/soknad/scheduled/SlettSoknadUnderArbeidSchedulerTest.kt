package no.nav.sosialhjelp.soknad.scheduled

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadService
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class SlettSoknadUnderArbeidSchedulerTest {
    private val leaderElection: LeaderElection = mockk()
    private val batchSoknadUnderArbeidRepository: BatchSoknadUnderArbeidRepository = mockk()
    private val mellomlagringService: MellomlagringService = mockk()

    private val scheduler = SlettSoknadUnderArbeidScheduler(
        batchEnabled = true,
        schedulerDisabled = false,
        leaderElection,
        batchSoknadUnderArbeidRepository,
        mellomlagringService
    )

    @BeforeEach
    fun setUp() {
        every { leaderElection.isLeader() } returns true
    }

    @Test
    fun skalSletteGamleSoknadUnderArbeid() {
        val soknadUnderArbeid1 = SoknadUnderArbeid(
            soknadId = 1L,
            versjon = 1L,
            behandlingsId = "behandlingsId1",
            tilknyttetBehandlingsId = null,
            eier = "11111111111",
            jsonInternalSoknad = SoknadService.createEmptyJsonInternalSoknad("11111111111"),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )

        val soknadUnderArbeid2 = SoknadUnderArbeid(
            soknadId = 2L,
            versjon = 2L,
            behandlingsId = "behandlingsId2",
            tilknyttetBehandlingsId = null,
            eier = "11111111111",
            jsonInternalSoknad = SoknadService.createEmptyJsonInternalSoknad("11111111111"),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )

        every { batchSoknadUnderArbeidRepository.hentGamleSoknadUnderArbeidForBatch() } returns listOf(soknadUnderArbeid1.soknadId, soknadUnderArbeid2.soknadId)
        every { batchSoknadUnderArbeidRepository.hentSoknadUnderArbeid(soknadUnderArbeid1.soknadId) } returns soknadUnderArbeid1
        every { batchSoknadUnderArbeidRepository.hentSoknadUnderArbeid(soknadUnderArbeid2.soknadId) } returns soknadUnderArbeid2
        every { batchSoknadUnderArbeidRepository.slettSoknad(any()) } just runs
        every { mellomlagringService.erMellomlagringEnabledOgSoknadSkalSendesMedDigisosApi(any()) } returns true
        every { mellomlagringService.deleteAllVedlegg(any()) } just runs

        scheduler.slettGamleSoknadUnderArbeid()

        verify(exactly = 2) { batchSoknadUnderArbeidRepository.slettSoknad(any()) }
        verify(exactly = 2) { mellomlagringService.deleteAllVedlegg(any()) }
    }
}
