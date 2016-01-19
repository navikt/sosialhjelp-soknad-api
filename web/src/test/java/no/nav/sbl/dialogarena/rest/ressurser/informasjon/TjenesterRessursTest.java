package no.nav.sbl.dialogarena.rest.ressurser.informasjon;


import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.AktivitetService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.MaalgrupperService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
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
    public void setUp() throws Exception {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        fodselsnummer = StaticSubjectHandler.getSubjectHandler().getUid();
    }

    @Test
    public void skalHenteAktiviteter() throws Exception {
        ressurs.hentAktiviteter();
        verify(aktivitetService).hentAktiviteter(fodselsnummer);
    }

    @Test
    public void skalHenteMaalgrupper() throws Exception {
        ressurs.hentMaalgrupper();
        verify(maalgrupperService).hentMaalgrupper(fodselsnummer);
    }
}