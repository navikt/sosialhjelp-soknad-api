package no.nav.sosialhjelp.soknad.innsending.svarut

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.FiksData
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.Oppgave
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.Status
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.innsending.InnsendingService
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class FiksHandtererTest {

    private val fiksSender: FiksSender = mockk()
    private val innsendingService: InnsendingService = mockk()
    private val prometheusMetricsService: PrometheusMetricsService = mockk(relaxed = true)
    private val fiksHandterer = FiksHandterer(fiksSender, innsendingService, prometheusMetricsService)

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        every { innsendingService.finnOgSlettSoknadUnderArbeidVedSendingTilFiks(any(), any()) } just runs
        every { innsendingService.oppdaterSoknadMetadataVedSendingTilFiks(any(), any(), any()) } just runs
    }

    @Test
    fun kjorerKjede() {
        every { innsendingService.hentSoknadMetadata(BEHANDLINGSID, AVSENDER) } returns lagSoknadMetadata()
        every { fiksSender.sendTilFiks(any()) } returns FIKSFORSENDELSEID
        val oppgave = opprettOppgave()

        fiksHandterer.eksekver(oppgave)
        verify(exactly = 1) { fiksSender.sendTilFiks(any()) }
        verify(exactly = 0) { innsendingService.finnOgSlettSoknadUnderArbeidVedSendingTilFiks(any(), any()) }
        verify(exactly = 0) { innsendingService.oppdaterSoknadMetadataVedSendingTilFiks(any(), any(), any()) }
        assertThat(oppgave.steg).isEqualTo(22)

        fiksHandterer.eksekver(oppgave)
        verify(exactly = 1) { innsendingService.finnOgSlettSoknadUnderArbeidVedSendingTilFiks(BEHANDLINGSID, AVSENDER) }
        verify(exactly = 0) { innsendingService.oppdaterSoknadMetadataVedSendingTilFiks(any(), any(), any()) }
        assertThat(oppgave.steg).isEqualTo(23)

        fiksHandterer.eksekver(oppgave)
        verify(exactly = 1) { innsendingService.oppdaterSoknadMetadataVedSendingTilFiks(any(), BEHANDLINGSID, AVSENDER) }
        assertThat(oppgave.status).isEqualTo(Status.FERDIG)
    }

    @Test
    fun lagrerFeilmelding() {
        every { innsendingService.hentSoknadMetadata(BEHANDLINGSID, AVSENDER) } returns lagSoknadMetadata()
        every { fiksSender.sendTilFiks(any()) } throws RuntimeException("feilmelding123")
        val oppgave = opprettOppgave()
        try {
            fiksHandterer.eksekver(oppgave)
            Assertions.fail<Any>("exception skal bli kastet videre")
        } catch (_: Exception) {
        }
        assertThat(oppgave.oppgaveResultat!!.feilmelding).isEqualTo("feilmelding123")
    }

    @Test
    fun kjorerKjedeSelvOmFeilerForsteGang() {
        // Feks. dersom en ettersendelse sin svarPaForsendelseId er null
        every { innsendingService.hentSoknadMetadata(BEHANDLINGSID, AVSENDER) } returns lagSoknadMetadataEttersendelse()
        every { fiksSender.sendTilFiks(any()) } throws IllegalStateException("Ettersendelse har svarPaForsendelseId null") andThen FIKSFORSENDELSEID
        val oppgave = opprettOppgave()
        try {
            fiksHandterer.eksekver(oppgave)
        } catch (ignored: IllegalStateException) {
        }
        assertThat(oppgave.oppgaveResultat!!.feilmelding).isEqualTo("Ettersendelse har svarPaForsendelseId null")
        verify(exactly = 1) { fiksSender.sendTilFiks(any()) }
        verify(exactly = 0) { innsendingService.finnOgSlettSoknadUnderArbeidVedSendingTilFiks(any(), any()) }
        verify(exactly = 0) { innsendingService.oppdaterSoknadMetadataVedSendingTilFiks(any(), any(), any()) }
        assertThat(oppgave.steg).isEqualTo(21)

        fiksHandterer.eksekver(oppgave)
        verify(exactly = 2) { fiksSender.sendTilFiks(any()) }
        verify(exactly = 0) { innsendingService.finnOgSlettSoknadUnderArbeidVedSendingTilFiks(any(), any()) }
        verify(exactly = 0) { innsendingService.oppdaterSoknadMetadataVedSendingTilFiks(any(), any(), any()) }

        assertThat(oppgave.steg).isEqualTo(22)
        fiksHandterer.eksekver(oppgave)
        verify(exactly = 1) { innsendingService.finnOgSlettSoknadUnderArbeidVedSendingTilFiks(BEHANDLINGSID, AVSENDER) }
        verify(exactly = 0) { innsendingService.oppdaterSoknadMetadataVedSendingTilFiks(any(), any(), any()) }

        assertThat(oppgave.steg).isEqualTo(23)
        fiksHandterer.eksekver(oppgave)
        verify(exactly = 1) { innsendingService.oppdaterSoknadMetadataVedSendingTilFiks(any(), BEHANDLINGSID, AVSENDER) }
        assertThat(oppgave.status).isEqualTo(Status.FERDIG)
    }

    private fun opprettOppgave(): Oppgave {
        return Oppgave(
            id = 0L,
            behandlingsId = BEHANDLINGSID,
            type = FiksHandterer.FIKS_OPPGAVE,
            status = Status.KLAR,
            steg = OppgaveHandtererImpl.FORSTE_STEG_NY_INNSENDING,
            oppgaveData = FiksData(avsenderFodselsnummer = AVSENDER),
            opprettet = LocalDateTime.now(),
            sistKjort = null,
            nesteForsok = LocalDateTime.now(),
            retries = 0
        )
    }

//    private fun lagSendtSoknad(): SendtSoknad {
//        return SendtSoknad(
//            behandlingsId = BEHANDLINGSID,
//            eier = AVSENDER,
//            orgnummer = "orgnr",
//            navEnhetsnavn = NAVENHETSNAVN,
//            brukerOpprettetDato = LocalDateTime.now(),
//            brukerFerdigDato = LocalDateTime.now(),
//            sendtDato = null
//        )
//    }

    private fun lagSoknadMetadata(): SoknadMetadata {
        return SoknadMetadata(
            id = 1L,
            behandlingsId = BEHANDLINGSID,
            fnr = AVSENDER,
            orgnr = "orgnr",
            navEnhet = NAVENHETSNAVN,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now(),
            innsendtDato = null
        )
    }

//    private fun lagSendtEttersendelse(): SendtSoknad {
//        val sendtSoknad = lagSendtSoknad()
//        sendtSoknad.tilknyttetBehandlingsId = "soknadId"
//        return sendtSoknad
//    }

    private fun lagSoknadMetadataEttersendelse(): SoknadMetadata {
        val soknadMetadata = lagSoknadMetadata()
        soknadMetadata.tilknyttetBehandlingsId = "soknadId"
        return soknadMetadata
    }

    companion object {
        private const val AVSENDER = "123456789010"
        private const val BEHANDLINGSID = "12345"
        private const val FIKSFORSENDELSEID = "9876"
        private const val NAVENHETSNAVN = "NAV Sagene"
    }
}
