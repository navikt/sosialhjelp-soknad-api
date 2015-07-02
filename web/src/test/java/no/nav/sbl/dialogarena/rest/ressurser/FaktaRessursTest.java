package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class FaktaRessursTest {

    @Mock
    FaktaService faktaService;

    @InjectMocks
    FaktaRessurs ressurs;

    @Test
    public void faktumSkalLagresHvisInnsendtFaktumMatcherOppgittFaktumId() {
        Faktum faktum = new Faktum().medFaktumId(1L);
        ressurs.lagreFaktum(1L, faktum);
        verify(faktaService).lagreSoknadsFelt(faktum);
    }

    @Test(expected = RuntimeException.class)
    public void lagringAvFaktumSkalKasteExceptionHvisInnsendtFaktumMatcherOppgittFaktumId() {
        ressurs.lagreFaktum(1L, new Faktum().medFaktumId(2L));
    }
}
