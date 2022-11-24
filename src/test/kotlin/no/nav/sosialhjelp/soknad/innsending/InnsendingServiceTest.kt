package no.nav.sosialhjelp.soknad.innsending

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDateTime

internal class InnsendingServiceTest {

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val opplastetVedleggRepository: OpplastetVedleggRepository = mockk()
    private val soknadUnderArbeidService: SoknadUnderArbeidService = mockk()
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()


    private val innsendingService = InnsendingService(
        soknadUnderArbeidRepository,
        opplastetVedleggRepository,
        soknadUnderArbeidService,
        soknadMetadataRepository,
        Clock.systemDefaultZone()
    )

    @BeforeEach
    fun setUp() {
        every { soknadMetadataRepository.hent(any()) } returns createSoknadMetadata()
    }

    @Test
    fun `oppdater innsendingStatus for SoknadUnderArbeid`() {
        every { soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.oppdaterInnsendingStatus(any(), any()) } just runs

        innsendingService.oppdaterSoknadUnderArbeid(createSoknadUnderArbeid())
        verify(exactly = 1) { soknadUnderArbeidRepository.oppdaterInnsendingStatus(any(), any()) }
    }

    @Test
    fun `finn FiksForsendelseId fra SoknadMetadata for Ettersendelse`() {
        val fiksForsendelseId = innsendingService.finnFiksForsendelseIdForEttersendelse(createSoknadUnderArbeidForEttersendelse())
        assertThat(fiksForsendelseId).isEqualTo(FIKSFORSENDELSEID)
    }

    @Test
    fun `kaster feil hvis SoknadMetadata mangler for ettersendelse`() {
        every { soknadMetadataRepository.hent(any()) } returns null

        assertThatExceptionOfType(IllegalStateException::class.java)
            .isThrownBy {
                innsendingService.finnFiksForsendelseIdForEttersendelse(createSoknadUnderArbeidForEttersendelse())
            }
    }

    private fun createSoknadUnderArbeid(): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            soknadId = SOKNAD_UNDER_ARBEID_ID,
            versjon = 1L,
            behandlingsId = BEHANDLINGSID,
            eier = EIER,
            jsonInternalSoknad = createJsonInternalSoknadWithOrgnrAndNavEnhetsnavn(),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = OPPRETTET_DATO,
            sistEndretDato = SIST_ENDRET_DATO
        )
    }

    private fun createJsonInternalSoknadWithOrgnrAndNavEnhetsnavn(): JsonInternalSoknad {
        return JsonInternalSoknad().withMottaker(
            JsonSoknadsmottaker()
                .withOrganisasjonsnummer(ORGNR)
                .withNavEnhetsnavn(NAVENHETSNAVN)
        )
    }

    private fun createSoknadUnderArbeidForEttersendelse(): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            soknadId = SOKNAD_UNDER_ARBEID_ID,
            versjon = 1L,
            behandlingsId = BEHANDLINGSID,
            tilknyttetBehandlingsId = TILKNYTTET_BEHANDLINGSID,
            eier = EIER,
            jsonInternalSoknad = null,
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = OPPRETTET_DATO,
            sistEndretDato = SIST_ENDRET_DATO
        )
    }

    private fun createSoknadMetadata(): SoknadMetadata {
        return SoknadMetadata(
            id = 0L,
            behandlingsId = BEHANDLINGSID,
            fnr = EIER,
            orgnr = ORGNR_METADATA,
            navEnhet = NAVENHETSNAVN_METADATA,
            fiksForsendelseId = FIKSFORSENDELSEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = SIST_ENDRET_DATO
        )
    }

    companion object {
        private const val SOKNAD_UNDER_ARBEID_ID = 1L
        private const val SENDT_SOKNAD_ID = 2L
        private const val EIER = "12345678910"
        private val VEDLEGGTYPE = OpplastetVedleggType("bostotte|annetboutgift")
        private const val BEHANDLINGSID = "1100001L"
        private const val TILKNYTTET_BEHANDLINGSID = "1100002K"
        private const val FIKSFORSENDELSEID = "12345"
        private const val ORGNR = "012345678"
        private const val ORGNR_METADATA = "8888"
        private const val NAVENHETSNAVN = "NAV Enhet"
        private const val NAVENHETSNAVN_METADATA = "NAV Enhet2"
        private val OPPRETTET_DATO = LocalDateTime.now().minusSeconds(50)
        private val SIST_ENDRET_DATO = LocalDateTime.now()
    }
}
