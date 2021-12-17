package no.nav.sosialhjelp.soknad.scheduled

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.client.leaderelection.LeaderElection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SlettSoknadUnderArbeidSchedulerTest {
    private val leaderElection: LeaderElection = mockk()
    private val batchSoknadUnderArbeidRepository: BatchSoknadUnderArbeidRepository = mockk()

    private val scheduler = SlettSoknadUnderArbeidScheduler(leaderElection, batchSoknadUnderArbeidRepository, true)

    @BeforeEach
    fun setUp() {
        every { leaderElection.isLeader() } returns true
    }

    @Test
    fun skalSletteGamleSoknadUnderArbeid() {
        every { batchSoknadUnderArbeidRepository.hentGamleSoknadUnderArbeidForBatch() } returns listOf(1L, 2L)
        every { batchSoknadUnderArbeidRepository.slettSoknad(any()) } just runs

        scheduler.slettGamleSoknadUnderArbeid()

        verify(exactly = 2) { batchSoknadUnderArbeidRepository.slettSoknad(any()) }
    }
}
