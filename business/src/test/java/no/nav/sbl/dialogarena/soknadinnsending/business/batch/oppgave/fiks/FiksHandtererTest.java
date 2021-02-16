package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.Oppgave;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sosialhjelp.soknad.domain.SendtSoknad;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandtererImpl.FORSTE_STEG_NY_INNSENDING;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FiksHandtererTest {

    private static final String AVSENDER = "123456789010";
    private static final String BEHANDLINGSID = "12345";
    private static final String FIKSFORSENDELSEID = "9876";
    private static final String NAVENHETSNAVN = "NAV Sagene";

    @Mock
    FiksSender fiksSender;

    @Mock
    InnsendingService innsendingService;

    @InjectMocks
    FiksHandterer fiksHandterer;

    @Test
    public void kjorerKjede() {
        when(innsendingService.hentSendtSoknad(eq(BEHANDLINGSID), eq(AVSENDER))).thenReturn(lagSendtSoknad());
        when(fiksSender.sendTilFiks(any(SendtSoknad.class))).thenReturn(FIKSFORSENDELSEID);
        Oppgave oppgave = opprettOppgave();

        fiksHandterer.eksekver(oppgave);

        verify(fiksSender, times(1)).sendTilFiks(any(SendtSoknad.class));
        verify(innsendingService, never()).finnOgSlettSoknadUnderArbeidVedSendingTilFiks(anyString(), anyString());
        verify(innsendingService, never()).oppdaterSendtSoknadVedSendingTilFiks(anyString(), anyString(), anyString());
        assertThat(oppgave.steg, is(22));

        fiksHandterer.eksekver(oppgave);
        verify(innsendingService, times(1)).finnOgSlettSoknadUnderArbeidVedSendingTilFiks(eq(BEHANDLINGSID),
                eq(AVSENDER));
        verify(innsendingService, never()).oppdaterSendtSoknadVedSendingTilFiks(anyString(), anyString(), anyString());
        assertThat(oppgave.steg, is(23));

        fiksHandterer.eksekver(oppgave);
        verify(innsendingService, times(1))
                .oppdaterSendtSoknadVedSendingTilFiks(anyString(), eq(BEHANDLINGSID), eq(AVSENDER));
        assertThat(oppgave.status, is(Oppgave.Status.FERDIG));

    }

    @Test
    public void lagrerFeilmelding() {
        when(innsendingService.hentSendtSoknad(eq(BEHANDLINGSID), eq(AVSENDER))).thenReturn(new SendtSoknad());
        when(fiksSender.sendTilFiks(any(SendtSoknad.class))).thenThrow(new RuntimeException("feilmelding123"));
        Oppgave oppgave = opprettOppgave();

        try {
            fiksHandterer.eksekver(oppgave);
            fail("exception skal bli kastet videre");
        } catch (Exception e) {
        }

        assertThat(oppgave.oppgaveResultat.feilmelding, is("feilmelding123"));
    }

    @Test
    public void kjorerKjedeSelvOmFeilerForsteGang() {
        //Feks. dersom en ettersendelse sin svarPaForsendelseId er null
        when(innsendingService.hentSendtSoknad(eq(BEHANDLINGSID), eq(AVSENDER))).thenReturn(lagSendtEttersendelse());
        when(fiksSender.sendTilFiks(any(SendtSoknad.class)))
                .thenThrow(new IllegalStateException("Ettersendelse har svarPaForsendelseId null"))
                .thenReturn(FIKSFORSENDELSEID);

        Oppgave oppgave = opprettOppgave();

        try {
            fiksHandterer.eksekver(oppgave);
        } catch (IllegalStateException ignored) { }
        assertThat(oppgave.oppgaveResultat.feilmelding, is("Ettersendelse har svarPaForsendelseId null"));
        verify(fiksSender, times(1)).sendTilFiks(any(SendtSoknad.class));
        verify(innsendingService, never()).finnOgSlettSoknadUnderArbeidVedSendingTilFiks(anyString(), anyString());
        verify(innsendingService, never()).oppdaterSendtSoknadVedSendingTilFiks(anyString(), anyString(), anyString());
        assertThat(oppgave.steg, is(21));

        fiksHandterer.eksekver(oppgave);
        verify(fiksSender, times(2)).sendTilFiks(any(SendtSoknad.class));
        verify(innsendingService, never()).finnOgSlettSoknadUnderArbeidVedSendingTilFiks(anyString(), anyString());
        verify(innsendingService, never()).oppdaterSendtSoknadVedSendingTilFiks(anyString(), anyString(), anyString());
        assertThat(oppgave.steg, is(22));

        fiksHandterer.eksekver(oppgave);
        verify(innsendingService, times(1)).finnOgSlettSoknadUnderArbeidVedSendingTilFiks(eq(BEHANDLINGSID),
                eq(AVSENDER));
        verify(innsendingService, never()).oppdaterSendtSoknadVedSendingTilFiks(anyString(), anyString(), anyString());
        assertThat(oppgave.steg, is(23));

        fiksHandterer.eksekver(oppgave);
        verify(innsendingService, times(1))
                .oppdaterSendtSoknadVedSendingTilFiks(anyString(), eq(BEHANDLINGSID), eq(AVSENDER));
        assertThat(oppgave.status, is(Oppgave.Status.FERDIG));
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