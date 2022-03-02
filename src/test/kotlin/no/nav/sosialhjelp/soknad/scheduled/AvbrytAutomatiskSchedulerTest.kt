package no.nav.sosialhjelp.soknad.scheduled

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.BatchSoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.UNDER_ARBEID
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Optional

internal class AvbrytAutomatiskSchedulerTest {
    private val leaderElection: LeaderElection = mockk()
    private val batchSoknadUnderArbeidRepository: BatchSoknadUnderArbeidRepository = mockk()
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val batchSoknadMetadataRepository: BatchSoknadMetadataRepository = mockk()

    private val scheduler = AvbrytAutomatiskScheduler(
        leaderElection,
        soknadMetadataRepository,
        batchSoknadMetadataRepository,
        batchSoknadUnderArbeidRepository,
        batchEnabled = true,
        schedulerDisabled = false
    )

    @BeforeEach
    fun setup() {
        every { leaderElection.isLeader() } returns true
        every { soknadMetadataRepository.hentNesteId() } returns 123L
    }

    @Test
    fun avbrytAutomatiskOgSlettGamleSoknader() {
        val soknadMetadata = soknadMetadata(BEHANDLINGS_ID, UNDER_ARBEID, DAGER_GAMMEL_SOKNAD + 1)
        val soknadUnderArbeid = SoknadUnderArbeid()
            .withSoknadId(1L)
            .withEier(EIER)
            .withBehandlingsId(BEHANDLINGS_ID)
            .withStatus(SoknadUnderArbeidStatus.UNDER_ARBEID)

        every {
            batchSoknadMetadataRepository.hentForBatch(DAGER_GAMMEL_SOKNAD)
        } returns Optional.of(soknadMetadata) andThen Optional.empty()
        every {
            batchSoknadUnderArbeidRepository.hentSoknadUnderArbeidIdFromBehandlingsIdOptional(BEHANDLINGS_ID)
        } returns Optional.of(soknadUnderArbeid.soknadId)

        val soknadMetadataSlot = slot<SoknadMetadata>()
        every { soknadMetadataRepository.oppdater(capture(soknadMetadataSlot)) } just runs

        scheduler.avbrytGamleSoknader()

        verify { soknadMetadataRepository.oppdater(soknadMetadataSlot.captured) }
        val oppdatertSoknadMetadata = soknadMetadataSlot.captured
        assertThat(oppdatertSoknadMetadata.status)
            .isEqualTo(SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK)
        verify { batchSoknadUnderArbeidRepository.slettSoknad(any()) }
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
        meta.type = SoknadType.SEND_SOKNAD_KOMMUNAL
        meta.skjema = ""
        meta.status = status
        meta.innsendtDato = LocalDateTime.now().minusDays(dagerSiden.toLong())
        meta.opprettetDato = LocalDateTime.now().minusDays(dagerSiden.toLong())
        meta.sistEndretDato = LocalDateTime.now().minusDays(dagerSiden.toLong())
        return meta
    }

    companion object {
        private const val EIER = "11111111111"
        private const val BEHANDLINGS_ID = "1100AAAAA"
        private const val DAGER_GAMMEL_SOKNAD = 14
    }
}
