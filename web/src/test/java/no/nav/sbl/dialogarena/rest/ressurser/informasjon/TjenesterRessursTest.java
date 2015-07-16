package no.nav.sbl.dialogarena.rest.ressurser.informasjon;


import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.AktiviteterService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TjenesterRessursTest {

    private String fodselsnummer;
    @InjectMocks
    TjenesterRessurs ressurs;

    @Mock
    AktiviteterService aktiviteterService;

    @Before
    public void setUp() throws Exception {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        fodselsnummer = StaticSubjectHandler.getSubjectHandler().getUid();
    }

    @Test
    public void skalHenteAktiviteter() throws Exception {
        ressurs.hentAktiviteter();
        verify(aktiviteterService).hentAktiviteter(fodselsnummer);
    }

    @Test
    public void skalHenteMaalgrupper() throws Exception {
        assertThat(ressurs.hentMaalgrupper()).hasSize(2);
    }
}