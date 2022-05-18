package no.nav.sosialhjelp.soknad.scheduled

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.BatchSoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus.UNDER_ARBEID
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataType
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadService
import no.nav.sosialhjelp.soknad.scheduled.leaderelection.LeaderElection
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class AvbrytAutomatiskSchedulerTest {
    private val leaderElection: LeaderElection = mockk()
    private val batchSoknadUnderArbeidRepository: BatchSoknadUnderArbeidRepository = mockk()
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val batchSoknadMetadataRepository: BatchSoknadMetadataRepository = mockk()
    private val mellomlagringService: MellomlagringService = mockk()

    private val scheduler = AvbrytAutomatiskScheduler(
        batchEnabled = true,
        schedulerDisabled = false,
        leaderElection,
        soknadMetadataRepository,
        batchSoknadMetadataRepository,
        batchSoknadUnderArbeidRepository,
        mellomlagringService,
    )

    @BeforeEach
    fun setup() {
        every { leaderElection.isLeader() } returns true
        every { soknadMetadataRepository.hentNesteId() } returns 123L
    }

    @Test
    fun avbrytAutomatiskOgSlettGamleSoknader() {
        val soknadMetadata = soknadMetadata(BEHANDLINGS_ID, UNDER_ARBEID, DAGER_GAMMEL_SOKNAD + 1)
        val soknadUnderArbeid = SoknadUnderArbeid(
            soknadId = 1L,
            versjon = 1L,
            behandlingsId = BEHANDLINGS_ID,
            tilknyttetBehandlingsId = null,
            eier = "11111111111",
            jsonInternalSoknad = null,
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )

        every {
            batchSoknadMetadataRepository.hentForBatch(DAGER_GAMMEL_SOKNAD)
        } returns soknadMetadata andThen null

        every {
            batchSoknadUnderArbeidRepository.hentSoknadUnderArbeid(BEHANDLINGS_ID)
        } returns soknadUnderArbeid

        val soknadMetadataSlot = slot<SoknadMetadata>()
        every { soknadMetadataRepository.oppdater(capture(soknadMetadataSlot)) } just runs

        every { mellomlagringService.erMellomlagringEnabledOgSoknadSkalSendesMedDigisosApi(soknadUnderArbeid) } returns false

        scheduler.avbrytGamleSoknader()

        verify { soknadMetadataRepository.oppdater(soknadMetadataSlot.captured) }
        val oppdatertSoknadMetadata = soknadMetadataSlot.captured
        assertThat(oppdatertSoknadMetadata.status)
            .isEqualTo(SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK)
        verify { batchSoknadUnderArbeidRepository.slettSoknad(any()) }
        verify(exactly = 0) { mellomlagringService.deleteAllVedlegg(any()) }
    }

    @Test
    fun avbrytAutomatiskOgSlettGamleSoknaderMedMellomlagredeVedlegg() {
        val soknadMetadata = soknadMetadata(BEHANDLINGS_ID, UNDER_ARBEID, DAGER_GAMMEL_SOKNAD + 1)
        val soknadUnderArbeid = SoknadUnderArbeid(
            soknadId = 1L,
            versjon = 1L,
            behandlingsId = BEHANDLINGS_ID,
            tilknyttetBehandlingsId = null,
            eier = "11111111111",
            jsonInternalSoknad = SoknadService.createEmptyJsonInternalSoknad("11111111111"),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )

        soknadUnderArbeid.jsonInternalSoknad?.soknad?.mottaker?.kommunenummer = "1234"

        every {
            batchSoknadMetadataRepository.hentForBatch(DAGER_GAMMEL_SOKNAD)
        } returns soknadMetadata andThen null
        every {
            batchSoknadUnderArbeidRepository.hentSoknadUnderArbeid(BEHANDLINGS_ID)
        } returns soknadUnderArbeid

        val soknadMetadataSlot = slot<SoknadMetadata>()
        every { soknadMetadataRepository.oppdater(capture(soknadMetadataSlot)) } just runs

        every { mellomlagringService.erMellomlagringEnabledOgSoknadSkalSendesMedDigisosApi(soknadUnderArbeid) } returns true
        every { mellomlagringService.deleteAllVedlegg(any()) } just runs

        scheduler.avbrytGamleSoknader()

        verify { soknadMetadataRepository.oppdater(soknadMetadataSlot.captured) }
        val oppdatertSoknadMetadata = soknadMetadataSlot.captured
        assertThat(oppdatertSoknadMetadata.status)
            .isEqualTo(SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK)
        verify { batchSoknadUnderArbeidRepository.slettSoknad(any()) }
        verify(exactly = 1) { mellomlagringService.deleteAllVedlegg(any()) }
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
        private const val BEHANDLINGS_ID = "1100AAAAA"
        private const val DAGER_GAMMEL_SOKNAD = 14
    }
}
