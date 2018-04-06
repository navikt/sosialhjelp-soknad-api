package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.Oppgave;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FillagerService;
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

    @Mock
    FiksSender fiksSender;

    @Mock
    MetadataInnfyller metadataInnfyller;

    @Mock
    FillagerService fillagerService;

    @InjectMocks
    FiksHandterer fiksHandterer;


    @Test
    public void kjorerKjede() {

        Oppgave oppgave = new Oppgave();
        oppgave.behandlingsId = "12345";

        fiksHandterer.eksekver(oppgave);

        verify(metadataInnfyller, times(1)).byggOppFiksData(any());
        verify(fillagerService, times(0)).slettAlle(any());
        verify(fiksSender, times(0)).sendTilFiks(any());
        verify(metadataInnfyller, times(0)).lagreFiksId(any(), any());
        assertEquals(1, oppgave.steg);

        fiksHandterer.eksekver(oppgave);
        verify(fillagerService, times(0)).slettAlle(any());
        verify(fiksSender, times(1)).sendTilFiks(any());
        verify(metadataInnfyller, times(0)).lagreFiksId(any(), any());
        assertEquals(2, oppgave.steg);

        fiksHandterer.eksekver(oppgave);
        verify(fillagerService, times(1)).slettAlle(any());
        verify(metadataInnfyller, times(0)).lagreFiksId(any(), any());
        assertEquals(3, oppgave.steg);

        fiksHandterer.eksekver(oppgave);
        verify(metadataInnfyller, times(1)).lagreFiksId(any(), any());
        assertEquals(Oppgave.Status.FERDIG, oppgave.status);

    }

    @Test
    public void lagrerFeilmelding() {
        when(fiksSender.sendTilFiks(any())).thenThrow(new RuntimeException("feilmelding123"));

        Oppgave oppgave = new Oppgave();
        oppgave.behandlingsId = "12345";
        oppgave.steg = 1;

        try {
            fiksHandterer.eksekver(oppgave);
            fail("exception skal bli kastet videre");
        } catch (Exception e) {
        }

        assertEquals("feilmelding123", oppgave.oppgaveResultat.feilmelding);
    }
}