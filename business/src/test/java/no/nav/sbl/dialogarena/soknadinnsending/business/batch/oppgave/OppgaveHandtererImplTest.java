package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave;

import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.oppgave.OppgaveRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.Oppgave.Status.UNDER_ARBEID;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OppgaveHandtererImplTest {

    @Mock
    private
    FiksHandterer fiksHandterer;

    @Mock
    private
    OppgaveRepository oppgaveRepository;

    @InjectMocks
    OppgaveHandtererImpl oppgaveHandterer;

    @Captor
    ArgumentCaptor<Oppgave> capturedOppgave;

    @Test
    public void prosessereFeilendeOppgaveSkalSetteNesteForsok() {
        Oppgave oppgave = new Oppgave();
        oppgave.status = UNDER_ARBEID;

        when(oppgaveRepository.hentNeste())
                .thenReturn(Optional.of(oppgave))
                .thenReturn(Optional.empty());

        doThrow(new IllegalStateException()).when(fiksHandterer).eksekver(oppgave);

        oppgaveHandterer.prosesserOppgaver();

        verify(oppgaveRepository, times(1)).oppdater(capturedOppgave.capture());
        assertEquals(Oppgave.Status.KLAR, capturedOppgave.getValue().status);
        assertNotNull(capturedOppgave.getValue().nesteForsok);
    }
}