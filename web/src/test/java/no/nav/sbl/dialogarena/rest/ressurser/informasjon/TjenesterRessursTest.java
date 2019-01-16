package no.nav.sbl.dialogarena.rest.ressurser.informasjon;


import no.nav.sbl.dialogarena.sendsoknad.domain.util.StaticOidcSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.AktivitetService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.MaalgrupperService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.lang.System.setProperty;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.OidcSubjectHandler.OIDC_SUBJECT_HANDLER_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.OidcSubjectHandler.getSubjectHandler;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TjenesterRessursTest {

    private String fodselsnummer;
    @InjectMocks
    private TjenesterRessurs ressurs;

    @Mock
    private AktivitetService aktivitetService;

    @Mock
    private MaalgrupperService maalgrupperService;

    @Before
    public void setUp() {
        setProperty(OIDC_SUBJECT_HANDLER_KEY, StaticOidcSubjectHandler.class.getName());
        fodselsnummer = getSubjectHandler().getUserIdFromToken();
    }

    @Test
    public void skalHenteAktiviteter() {
        ressurs.hentAktiviteter();
        verify(aktivitetService).hentAktiviteter(fodselsnummer);
    }

    @Test
    public void skalHenteMaalgrupper() {
        ressurs.hentMaalgrupper();
        verify(maalgrupperService).hentMaalgrupper(fodselsnummer);
    }
}