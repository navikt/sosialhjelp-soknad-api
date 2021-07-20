package no.nav.sosialhjelp.soknad.business.batch.oppgave;

import no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks.FiksHandterer;
import no.nav.sosialhjelp.soknad.business.db.repositories.oppgave.OppgaveRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static no.nav.sosialhjelp.soknad.business.batch.oppgave.Oppgave.Status.UNDER_ARBEID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OppgaveHandtererImplTest {

    @Mock
    private FiksHandterer fiksHandterer;

    @Mock
    private OppgaveRepository oppgaveRepository;

    @InjectMocks
    private OppgaveHandtererImpl oppgaveHandterer;

    @Captor
    private ArgumentCaptor<Oppgave> capturedOppgave;

    @Test
    void prosessereFeilendeOppgaveSkalSetteNesteForsok() {
        Oppgave oppgave = new Oppgave();
        oppgave.status = UNDER_ARBEID;

        when(oppgaveRepository.hentNeste())
                .thenReturn(Optional.of(oppgave))
                .thenReturn(Optional.empty());

        doThrow(new IllegalStateException()).when(fiksHandterer).eksekver(oppgave);

        oppgaveHandterer.prosesserOppgaver();

        verify(oppgaveRepository, times(1)).oppdater(capturedOppgave.capture());
        assertThat(capturedOppgave.getValue().status).isEqualTo(Oppgave.Status.KLAR);
        assertThat(capturedOppgave.getValue().nesteForsok).isNotNull();
    }
}