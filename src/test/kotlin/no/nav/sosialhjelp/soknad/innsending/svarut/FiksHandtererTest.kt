package no.nav.sosialhjelp.soknad.innsending.svarut

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.sosialhjelp.soknad.business.batch.oppgave.Oppgave
import no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks.FiksData
import no.nav.sosialhjelp.soknad.domain.SendtSoknad
import no.nav.sosialhjelp.soknad.innsending.InnsendingService
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class FiksHandtererTest {

    private val fiksSender: FiksSender = mockk()
    private val innsendingService: InnsendingService = mockk()
    private val fiksHandterer = FiksHandterer(fiksSender, innsendingService)

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        every { innsendingService.finnOgSlettSoknadUnderArbeidVedSendingTilFiks(any(), any()) } just runs
        every { innsendingService.oppdaterSendtSoknadVedSendingTilFiks(any(), any(), any()) } just runs
    }

    @Test
    fun kjorerKjede() {
        every { innsendingService.hentSendtSoknad(BEHANDLINGSID, AVSENDER) } returns lagSendtSoknad()
        every { fiksSender.sendTilFiks(any()) } returns FIKSFORSENDELSEID
        val oppgave = opprettOppgave()

        fiksHandterer.eksekver(oppgave)
        verify(exactly = 1) { fiksSender.sendTilFiks(any()) }
        verify(exactly = 0) { innsendingService.finnOgSlettSoknadUnderArbeidVedSendingTilFiks(any(), any()) }
        verify(exactly = 0) { innsendingService.oppdaterSendtSoknadVedSendingTilFiks(any(), any(), any()) }
        assertThat(oppgave.steg).isEqualTo(22)

        fiksHandterer.eksekver(oppgave)
        verify(exactly = 1) { innsendingService.finnOgSlettSoknadUnderArbeidVedSendingTilFiks(BEHANDLINGSID, AVSENDER) }
        verify(exactly = 0) { innsendingService.oppdaterSendtSoknadVedSendingTilFiks(any(), any(), any()) }
        assertThat(oppgave.steg).isEqualTo(23)

        fiksHandterer.eksekver(oppgave)
        verify(exactly = 1) { innsendingService.oppdaterSendtSoknadVedSendingTilFiks(any(), BEHANDLINGSID, AVSENDER) }
        assertThat(oppgave.status).isEqualTo(Oppgave.Status.FERDIG)
    }

    @Test
    fun lagrerFeilmelding() {
        every { innsendingService.hentSendtSoknad(BEHANDLINGSID, AVSENDER) } returns SendtSoknad()
        every { fiksSender.sendTilFiks(any()) } throws RuntimeException("feilmelding123")
        val oppgave = opprettOppgave()
        try {
            fiksHandterer.eksekver(oppgave)
            Assertions.fail<Any>("exception skal bli kastet videre")
        } catch (_: Exception) {
        }
        assertThat(oppgave.oppgaveResultat.feilmelding).isEqualTo("feilmelding123")
    }

    @Test
    fun kjorerKjedeSelvOmFeilerForsteGang() {
        // Feks. dersom en ettersendelse sin svarPaForsendelseId er null
        every { innsendingService.hentSendtSoknad(BEHANDLINGSID, AVSENDER) } returns lagSendtEttersendelse()
        every { fiksSender.sendTilFiks(any()) } throws IllegalStateException("Ettersendelse har svarPaForsendelseId null") andThen FIKSFORSENDELSEID
        val oppgave = opprettOppgave()
        try {
            fiksHandterer.eksekver(oppgave)
        } catch (ignored: IllegalStateException) {
        }
        assertThat(oppgave.oppgaveResultat.feilmelding).isEqualTo("Ettersendelse har svarPaForsendelseId null")
        verify(exactly = 1) { fiksSender.sendTilFiks(any()) }
        verify(exactly = 0) { innsendingService.finnOgSlettSoknadUnderArbeidVedSendingTilFiks(any(), any()) }
        verify(exactly = 0) { innsendingService.oppdaterSendtSoknadVedSendingTilFiks(any(), any(), any()) }
        assertThat(oppgave.steg).isEqualTo(21)

        fiksHandterer.eksekver(oppgave)
        verify(exactly = 2) { fiksSender.sendTilFiks(any()) }
        verify(exactly = 0) { innsendingService.finnOgSlettSoknadUnderArbeidVedSendingTilFiks(any(), any()) }
        verify(exactly = 0) { innsendingService.oppdaterSendtSoknadVedSendingTilFiks(any(), any(), any()) }

        assertThat(oppgave.steg).isEqualTo(22)
        fiksHandterer.eksekver(oppgave)
        verify(exactly = 1) { innsendingService.finnOgSlettSoknadUnderArbeidVedSendingTilFiks(BEHANDLINGSID, AVSENDER) }
        verify(exactly = 0) { innsendingService.oppdaterSendtSoknadVedSendingTilFiks(any(), any(), any()) }

        assertThat(oppgave.steg).isEqualTo(23)
        fiksHandterer.eksekver(oppgave)
        verify(exactly = 1) { innsendingService.oppdaterSendtSoknadVedSendingTilFiks(any(), BEHANDLINGSID, AVSENDER) }
        assertThat(oppgave.status).isEqualTo(Oppgave.Status.FERDIG)
    }

    private fun opprettOppgave(): Oppgave {
        val oppgave = Oppgave()
        oppgave.behandlingsId = BEHANDLINGSID
        val oppgaveData = FiksData()
        oppgaveData.avsenderFodselsnummer = AVSENDER
        oppgave.oppgaveData = oppgaveData
        oppgave.steg = OppgaveHandtererImpl.FORSTE_STEG_NY_INNSENDING
        return oppgave
    }

    private fun lagSendtSoknad(): SendtSoknad {
        return SendtSoknad()
            .withEier(AVSENDER)
            .withBehandlingsId(BEHANDLINGSID)
            .withNavEnhetsnavn(NAVENHETSNAVN)
    }

    private fun lagSendtEttersendelse(): SendtSoknad {
        return lagSendtSoknad().withTilknyttetBehandlingsId("soknadId")
    }

    companion object {
        private const val AVSENDER = "123456789010"
        private const val BEHANDLINGSID = "12345"
        private const val FIKSFORSENDELSEID = "9876"
        private const val NAVENHETSNAVN = "NAV Sagene"
    }
}
