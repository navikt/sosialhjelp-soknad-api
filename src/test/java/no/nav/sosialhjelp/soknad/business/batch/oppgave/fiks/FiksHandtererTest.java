package no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks;

import no.nav.sosialhjelp.soknad.business.batch.oppgave.Oppgave;
import no.nav.sosialhjelp.soknad.domain.SendtSoknad;
import no.nav.sosialhjelp.soknad.innsending.InnsendingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.sosialhjelp.soknad.business.batch.oppgave.OppgaveHandtererImpl.FORSTE_STEG_NY_INNSENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiksHandtererTest {

    private static final String AVSENDER = "123456789010";
    private static final String BEHANDLINGSID = "12345";
    private static final String FIKSFORSENDELSEID = "9876";
    private static final String NAVENHETSNAVN = "NAV Sagene";

    @Mock
    private FiksSender fiksSender;

    @Mock
    private InnsendingService innsendingService;

    @InjectMocks
    private FiksHandterer fiksHandterer;

    @Test
    void kjorerKjede() {
        when(innsendingService.hentSendtSoknad(BEHANDLINGSID, AVSENDER)).thenReturn(lagSendtSoknad());
        when(fiksSender.sendTilFiks(any(SendtSoknad.class))).thenReturn(FIKSFORSENDELSEID);
        Oppgave oppgave = opprettOppgave();

        fiksHandterer.eksekver(oppgave);

        verify(fiksSender, times(1)).sendTilFiks(any(SendtSoknad.class));
        verify(innsendingService, never()).finnOgSlettSoknadUnderArbeidVedSendingTilFiks(anyString(), anyString());
        verify(innsendingService, never()).oppdaterSendtSoknadVedSendingTilFiks(anyString(), anyString(), anyString());
        assertThat(oppgave.steg).isEqualTo(22);

        fiksHandterer.eksekver(oppgave);
        verify(innsendingService, times(1)).finnOgSlettSoknadUnderArbeidVedSendingTilFiks(BEHANDLINGSID, AVSENDER);
        verify(innsendingService, never()).oppdaterSendtSoknadVedSendingTilFiks(anyString(), anyString(), anyString());
        assertThat(oppgave.steg).isEqualTo(23);

        fiksHandterer.eksekver(oppgave);
        verify(innsendingService, times(1))
                .oppdaterSendtSoknadVedSendingTilFiks(anyString(), eq(BEHANDLINGSID), eq(AVSENDER));
        assertThat(oppgave.status).isEqualTo(Oppgave.Status.FERDIG);

    }

    @Test
    void lagrerFeilmelding() {
        when(innsendingService.hentSendtSoknad(BEHANDLINGSID, AVSENDER)).thenReturn(new SendtSoknad());
        when(fiksSender.sendTilFiks(any(SendtSoknad.class))).thenThrow(new RuntimeException("feilmelding123"));
        Oppgave oppgave = opprettOppgave();

        try {
            fiksHandterer.eksekver(oppgave);
            fail("exception skal bli kastet videre");
        } catch (Exception e) {
        }

        assertThat(oppgave.oppgaveResultat.feilmelding).isEqualTo("feilmelding123");
    }

    @Test
    void kjorerKjedeSelvOmFeilerForsteGang() {
        //Feks. dersom en ettersendelse sin svarPaForsendelseId er null
        when(innsendingService.hentSendtSoknad(BEHANDLINGSID, AVSENDER)).thenReturn(lagSendtEttersendelse());
        when(fiksSender.sendTilFiks(any(SendtSoknad.class)))
                .thenThrow(new IllegalStateException("Ettersendelse har svarPaForsendelseId null"))
                .thenReturn(FIKSFORSENDELSEID);

        Oppgave oppgave = opprettOppgave();

        try {
            fiksHandterer.eksekver(oppgave);
        } catch (IllegalStateException ignored) { }
        assertThat(oppgave.oppgaveResultat.feilmelding).isEqualTo("Ettersendelse har svarPaForsendelseId null");
        verify(fiksSender, times(1)).sendTilFiks(any(SendtSoknad.class));
        verify(innsendingService, never()).finnOgSlettSoknadUnderArbeidVedSendingTilFiks(anyString(), anyString());
        verify(innsendingService, never()).oppdaterSendtSoknadVedSendingTilFiks(anyString(), anyString(), anyString());
        assertThat(oppgave.steg).isEqualTo(21);

        fiksHandterer.eksekver(oppgave);
        verify(fiksSender, times(2)).sendTilFiks(any(SendtSoknad.class));
        verify(innsendingService, never()).finnOgSlettSoknadUnderArbeidVedSendingTilFiks(anyString(), anyString());
        verify(innsendingService, never()).oppdaterSendtSoknadVedSendingTilFiks(anyString(), anyString(), anyString());
        assertThat(oppgave.steg).isEqualTo(22);

        fiksHandterer.eksekver(oppgave);
        verify(innsendingService, times(1)).finnOgSlettSoknadUnderArbeidVedSendingTilFiks(BEHANDLINGSID, AVSENDER);
        verify(innsendingService, never()).oppdaterSendtSoknadVedSendingTilFiks(anyString(), anyString(), anyString());
        assertThat(oppgave.steg).isEqualTo(23);

        fiksHandterer.eksekver(oppgave);
        verify(innsendingService, times(1))
                .oppdaterSendtSoknadVedSendingTilFiks(anyString(), eq(BEHANDLINGSID), eq(AVSENDER));
        assertThat(oppgave.status).isEqualTo(Oppgave.Status.FERDIG);
    }

    private Oppgave opprettOppgave() {
        Oppgave oppgave = new Oppgave();
        oppgave.behandlingsId = BEHANDLINGSID;
        FiksData oppgaveData = new FiksData();
        oppgaveData.avsenderFodselsnummer = AVSENDER;
        oppgave.oppgaveData = oppgaveData;
        oppgave.steg = FORSTE_STEG_NY_INNSENDING;
        return oppgave;
    }

    private SendtSoknad lagSendtSoknad() {
        return new SendtSoknad()
                .withEier(AVSENDER)
                .withBehandlingsId(BEHANDLINGSID)
                .withNavEnhetsnavn(NAVENHETSNAVN);
    }

    private SendtSoknad lagSendtEttersendelse() {
        return lagSendtSoknad().withTilknyttetBehandlingsId("soknadId");
    }
}