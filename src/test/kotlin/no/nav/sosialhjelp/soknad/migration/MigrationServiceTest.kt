package no.nav.sosialhjelp.soknad.migration

import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.sosialhjelp.soknad.repository.oppgave.Oppgave
import no.nav.sosialhjelp.soknad.repository.oppgave.OppgaveRepository
import no.nav.sosialhjelp.soknad.repository.oppgave.Status
import no.nav.sosialhjelp.soknad.repository.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.repository.opplastetvedlegg.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.migration.repo.OpplastetVedleggMigrationRepository
import no.nav.sosialhjelp.soknad.migration.repo.SoknadMetadataMigrationRepository
import no.nav.sosialhjelp.soknad.migration.repo.SoknadUnderArbeidMigrationRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class MigrationServiceTest {

    private val soknadMetadataMigrationRepository: SoknadMetadataMigrationRepository = mockk()
    private val soknadUnderArbeidMigrationRepository: SoknadUnderArbeidMigrationRepository = mockk()
    private val opplastetVedleggMigrationRepository: OpplastetVedleggMigrationRepository = mockk()
    private val oppgaveRepository: OppgaveRepository = mockk()

    private val migrationService = MigrationService(
        soknadMetadataMigrationRepository,
        soknadUnderArbeidMigrationRepository,
        opplastetVedleggMigrationRepository,
        oppgaveRepository
    )

    @Test
    internal fun `repository finner ingen nyere - returner null`() {
        every { soknadMetadataMigrationRepository.getNextSoknadMetadataAfter(any()) } returns null

        val next = migrationService.getNext(LocalDateTime.now())

        assertThat(next).isNull()
    }

    @Test
    internal fun `fullstendig replikering`() {
        val soknadMetadata = createSoknadMetadata("123")
        every { soknadMetadataMigrationRepository.getNextSoknadMetadataAfter(any()) } returns soknadMetadata

        val soknadUnderArbeid = createSoknadUnderArbeid("123")
        every { soknadUnderArbeidMigrationRepository.getSoknadUnderArbeid(any()) } returns soknadUnderArbeid
        every {
            opplastetVedleggMigrationRepository.getOpplastetVedlegg(soknadUnderArbeid.soknadId)
        } returns listOf(createOpplastetVedlegg(soknadUnderArbeid.soknadId))

        val oppgave = createOppgave("123")
        every { oppgaveRepository.hentOppgave(any()) } returns oppgave

        val next = migrationService.getNext(LocalDateTime.MIN)

        assertThat(next).isNotNull
        assertThat(next?.behandlingsId).isEqualTo("123")
        assertThat(next?.soknadMetadata?.behandlingsId).isEqualTo("123")
        assertThat(next?.soknadUnderArbeid).isNotNull
        assertThat(next?.soknadUnderArbeid?.opplastetVedleggList).hasSize(1)
        assertThat(next?.oppgave).isNotNull
    }

    @Test
    internal fun `soknadMetadata men ikke mer`() {
        val soknadMetadata = createSoknadMetadata("123")
        every { soknadMetadataMigrationRepository.getNextSoknadMetadataAfter(any()) } returns soknadMetadata
        every { soknadUnderArbeidMigrationRepository.getSoknadUnderArbeid(any()) } returns null
        every { oppgaveRepository.hentOppgave(any()) } returns null

        val next = migrationService.getNext(LocalDateTime.MIN)

        assertThat(next?.behandlingsId).isEqualTo("123")
        assertThat(next?.soknadMetadata?.behandlingsId).isEqualTo("123")
        assertThat(next?.soknadUnderArbeid).isNull()
        assertThat(next?.oppgave).isNull()

        verify { opplastetVedleggMigrationRepository wasNot called }
    }

    private fun createSoknadMetadata(behandlingsId: String): SoknadMetadata {
        return SoknadMetadata(
            1L, behandlingsId, null, "fnr", null, null, null, null, null, null, null, LocalDateTime.now(), LocalDateTime.now(), null, false
        )
    }

    private fun createSoknadUnderArbeid(behandlingsId: String): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            1L, 1L, behandlingsId, null, "fnr", null, SoknadUnderArbeidStatus.UNDER_ARBEID, LocalDateTime.now(), LocalDateTime.now()
        )
    }

    private fun createOpplastetVedlegg(soknadId: Long): OpplastetVedlegg {
        return OpplastetVedlegg(
            eier = "fnr",
            vedleggType = OpplastetVedleggType("annet|annet"),
            data = "hello".toByteArray(),
            soknadId = soknadId,
            filnavn = "filnavn",
            sha512 = "sha"
        )
    }

    private fun createOppgave(behandlingsId: String): Oppgave {
        return Oppgave(
            1L, behandlingsId, null, Status.UNDER_ARBEID, 21, null, null, null, null, null, 0
        )
    }
}
