package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.Oppgave;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FillagerService;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FiksHandtererTest {

    private static final String AVSENDER = "123456789010";
    private static final String BEHANDLINGSID = "12345";
    private static final String FIKSFORSENDELSEID = "9876";

    @Mock
    FiksSender fiksSender;

    @Mock
    MetadataInnfyller metadataInnfyller;

    @Mock
    FillagerService fillagerService;

    @Mock
    SendtSoknadRepository sendtSoknadRepository;

    @InjectMocks
    FiksHandterer fiksHandterer;

    @Test
    public void kjorerKjede() {
        when(fiksSender.sendTilFiks(any())).thenReturn(FIKSFORSENDELSEID);
        Oppgave oppgave = opprettOppgave();

        fiksHandterer.eksekver(oppgave);

        verify(metadataInnfyller, times(1)).byggOppFiksData(any());
        verify(fillagerService, never()).slettAlle(any());
        verify(fiksSender, never()).sendTilFiks(any());
        verify(metadataInnfyller, never()).lagreFiksId(any(), any());
        verify(sendtSoknadRepository, never()).oppdaterSendtSoknadVedSendingTilFiks(anyString(), anyString(), anyString());
        assertEquals(1, oppgave.steg);

        fiksHandterer.eksekver(oppgave);
        verify(fillagerService, never()).slettAlle(any());
        verify(fiksSender, times(1)).sendTilFiks(any());
        verify(metadataInnfyller, never()).lagreFiksId(any(), any());
        verify(sendtSoknadRepository, never()).oppdaterSendtSoknadVedSendingTilFiks(anyString(), anyString(), anyString());
        assertEquals(2, oppgave.steg);

        fiksHandterer.eksekver(oppgave);
        verify(fillagerService, times(1)).slettAlle(any());
        verify(metadataInnfyller, never()).lagreFiksId(any(), any());
        verify(sendtSoknadRepository, never()).oppdaterSendtSoknadVedSendingTilFiks(anyString(), anyString(), anyString());
        assertEquals(3, oppgave.steg);

        fiksHandterer.eksekver(oppgave);
        verify(metadataInnfyller, times(1)).lagreFiksId(any(), any());
        verify(sendtSoknadRepository, times(1))
                .oppdaterSendtSoknadVedSendingTilFiks(anyString(), eq(BEHANDLINGSID), eq(AVSENDER));
        assertEquals(Oppgave.Status.FERDIG, oppgave.status);

    }

    @Test
    public void lagrerFeilmelding() {
        when(fiksSender.sendTilFiks(any())).thenThrow(new RuntimeException("feilmelding123"));

        Oppgave oppgave = new Oppgave();
        oppgave.behandlingsId = BEHANDLINGSID;
        oppgave.steg = 1;

        try {
            fiksHandterer.eksekver(oppgave);
            fail("exception skal bli kastet videre");
        } catch (Exception e) {
        }

        assertEquals("feilmelding123", oppgave.oppgaveResultat.feilmelding);
    }

    private Oppgave opprettOppgave() {
        Oppgave oppgave = new Oppgave();
        oppgave.behandlingsId = BEHANDLINGSID;
        FiksData oppgaveData = new FiksData();
        oppgaveData.avsenderFodselsnummer = AVSENDER;
        oppgave.oppgaveData = oppgaveData;
        return oppgave;
    }
}