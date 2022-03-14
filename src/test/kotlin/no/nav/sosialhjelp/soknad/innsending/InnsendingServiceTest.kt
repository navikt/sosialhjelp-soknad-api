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
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknadRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.domain.SendtSoknad
import no.nav.sosialhjelp.soknad.domain.SoknadMetadata
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.SimpleTransactionStatus
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.Optional

internal class InnsendingServiceTest {

    private val transactionTemplate: TransactionTemplate = mockk()
    private val sendtSoknadRepository: SendtSoknadRepository = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val opplastetVedleggRepository: OpplastetVedleggRepository = mockk()
    private val soknadUnderArbeidService: SoknadUnderArbeidService = mockk()
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()

    private val innsendingService = InnsendingService(
        transactionTemplate,
        sendtSoknadRepository,
        soknadUnderArbeidRepository,
        opplastetVedleggRepository,
        soknadUnderArbeidService,
        soknadMetadataRepository
    )

    @BeforeEach
    fun setUp() {
        every { transactionTemplate.execute(any<TransactionCallback<Any>>()) } answers {
            val args = it.invocation.args
            val arg = args[0] as TransactionCallbackWithoutResult
            arg.doInTransaction(SimpleTransactionStatus())
        }
        every { sendtSoknadRepository.opprettSendtSoknad(any(), any()) } returns SENDT_SOKNAD_ID
        every { sendtSoknadRepository.hentSendtSoknad(any(), any()) } returns createSendtSoknad()
    }

    @Test
    fun opprettSendtSoknadOppretterSendtSoknadOgVedleggstatus() {
        every { soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.oppdaterInnsendingStatus(any(), any()) } just runs

        innsendingService.opprettSendtSoknad(
            createSoknadUnderArbeid().withJsonInternalSoknad(
                createJsonInternalSoknadWithOrgnrAndNavEnhetsnavn()
            )
        )
        verify(exactly = 1) { soknadUnderArbeidRepository.oppdaterInnsendingStatus(any(), any()) }
        verify(exactly = 1) { sendtSoknadRepository.opprettSendtSoknad(any(), any()) }
    }

    @Test
    fun mapSoknadUnderArbeidTilSendtSoknadMapperInfoRiktig() {
        val sendtSoknad = innsendingService.mapSoknadUnderArbeidTilSendtSoknad(
            createSoknadUnderArbeid().withJsonInternalSoknad(
                createJsonInternalSoknadWithOrgnrAndNavEnhetsnavn()
            )
        )
        assertThat(sendtSoknad.behandlingsId).isEqualTo(BEHANDLINGSID)
        assertThat(sendtSoknad.tilknyttetBehandlingsId).isNull()
        assertThat(sendtSoknad.eier).isEqualTo(EIER)
        assertThat(sendtSoknad.orgnummer).isEqualTo(ORGNR)
        assertThat(sendtSoknad.navEnhetsnavn).isEqualTo(NAVENHETSNAVN)
        assertThat(sendtSoknad.brukerOpprettetDato).isEqualTo(OPPRETTET_DATO)
        assertThat(sendtSoknad.brukerFerdigDato).isEqualTo(SIST_ENDRET_DATO)
        assertThat(sendtSoknad.sendtDato).isNull()
        assertThat(sendtSoknad.fiksforsendelseId).isNull()
    }

    @Test
    fun mapSoknadUnderArbeidTilSendtSoknadKasterFeilHvisIkkeEttersendingOgMottakerinfoMangler() {
        val soknadUnderArbeid = createSoknadUnderArbeidUtenTilknyttetBehandlingsid().withJsonInternalSoknad(
            JsonInternalSoknad().withMottaker(null)
        )
        assertThatExceptionOfType(IllegalStateException::class.java)
            .isThrownBy { innsendingService.mapSoknadUnderArbeidTilSendtSoknad(soknadUnderArbeid) }
    }

    @Test
    fun finnSendtSoknadForEttersendelseHenterMottakerinfoFraSendtSoknadVedEttersendelse() {
        val sendtSoknad = innsendingService.finnSendtSoknadForEttersendelse(createSoknadUnderArbeidForEttersendelse())
        assertThat(sendtSoknad.orgnummer).isEqualTo(ORGNR)
        assertThat(sendtSoknad.navEnhetsnavn).isEqualTo(NAVENHETSNAVN)
        assertThat(sendtSoknad.tilknyttetBehandlingsId).isEqualTo(TILKNYTTET_BEHANDLINGSID)
    }

    @Test
    fun finnSendtSoknadForEttersendelseHenterInfoFraSoknadMetadataHvisSendtSoknadMangler() {
        every { sendtSoknadRepository.hentSendtSoknad(any(), any()) } returns Optional.empty()
        every { soknadMetadataRepository.hent(any()) } returns createSoknadMetadata()
        val soknadMedMottaksinfoFraMetadata =
            innsendingService.finnSendtSoknadForEttersendelse(createSoknadUnderArbeidForEttersendelse())
        assertThat(soknadMedMottaksinfoFraMetadata.orgnummer).isEqualTo(ORGNR_METADATA)
        assertThat(soknadMedMottaksinfoFraMetadata.navEnhetsnavn).isEqualTo(NAVENHETSNAVN_METADATA)
    }

    @Test
    fun finnSendtSoknadForEttersendelseKasterFeilHvisSendtSoknadOgMetadataManglerForEttersendelse() {
        every { sendtSoknadRepository.hentSendtSoknad(any(), any()) } returns Optional.empty()
        every { soknadMetadataRepository.hent(any()) } returns null

        assertThatExceptionOfType(IllegalStateException::class.java)
            .isThrownBy {
                innsendingService.finnSendtSoknadForEttersendelse(createSoknadUnderArbeidForEttersendelse())
            }
    }

    private fun createSoknadUnderArbeid(): SoknadUnderArbeid {
        return SoknadUnderArbeid()
            .withSoknadId(SOKNAD_UNDER_ARBEID_ID)
            .withBehandlingsId(BEHANDLINGSID)
            .withEier(EIER)
            .withOpprettetDato(OPPRETTET_DATO)
            .withSistEndretDato(SIST_ENDRET_DATO)
    }

    private fun createJsonInternalSoknadWithOrgnrAndNavEnhetsnavn(): JsonInternalSoknad {
        return JsonInternalSoknad().withMottaker(
            JsonSoknadsmottaker()
                .withOrganisasjonsnummer(ORGNR)
                .withNavEnhetsnavn(NAVENHETSNAVN)
        )
    }

    private fun createSoknadUnderArbeidForEttersendelse(): SoknadUnderArbeid {
        return SoknadUnderArbeid()
            .withSoknadId(SOKNAD_UNDER_ARBEID_ID)
            .withBehandlingsId(BEHANDLINGSID)
            .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
            .withEier(EIER)
            .withOpprettetDato(OPPRETTET_DATO)
            .withSistEndretDato(SIST_ENDRET_DATO)
    }

    private fun createSoknadUnderArbeidUtenTilknyttetBehandlingsid(): SoknadUnderArbeid {
        return SoknadUnderArbeid()
            .withSoknadId(SOKNAD_UNDER_ARBEID_ID)
            .withBehandlingsId(BEHANDLINGSID)
            .withEier(EIER)
            .withOpprettetDato(OPPRETTET_DATO)
            .withSistEndretDato(SIST_ENDRET_DATO)
    }

    private fun createSendtSoknad(): Optional<SendtSoknad> {
        return Optional.of(
            SendtSoknad().withEier(EIER)
                .withBehandlingsId(BEHANDLINGSID)
                .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
                .withFiksforsendelseId(FIKSFORSENDELSEID)
                .withOrgnummer(ORGNR)
                .withNavEnhetsnavn(NAVENHETSNAVN)
                .withBrukerOpprettetDato(OPPRETTET_DATO)
                .withBrukerFerdigDato(SIST_ENDRET_DATO)
                .withSendtDato(LocalDateTime.now())
        )
    }

    private fun createSoknadMetadata(): SoknadMetadata {
        val soknadMetadata = SoknadMetadata()
        soknadMetadata.orgnr = ORGNR_METADATA
        soknadMetadata.navEnhet = NAVENHETSNAVN_METADATA
        return soknadMetadata
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
