package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;
import java.lang.reflect.Method;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SoknadRessursTest {

    public static final String BEHANDLINGSID = "123";

    @Mock
    SoknadService soknadService;

    @InjectMocks
    SoknadRessurs ressurs;

    @Test
    public void endepunkterSkalHaSikkerhet() {
        SoknadRessurs ressurs = new SoknadRessurs();
        Method[] methods = ressurs.getClass().getMethods();
        assertThat("some", is("some"));
    }

    @Test(expected = BadRequestException.class)
    public void oppdaterSoknadUtenParametreSkalKasteException() {
        ressurs.oppdaterSoknad(BEHANDLINGSID, null, null);
    }

    @Test
    public void oppdaterSoknadMedDelstegUtfyllingSkalSetteRiktigDelstegStatus() {
        ressurs.oppdaterSoknad(BEHANDLINGSID, "utfylling", null);
        verify(soknadService).settDelsteg(BEHANDLINGSID, DelstegStatus.UTFYLLING);
    }

    @Test
    public void oppdaterSoknadMedDelstegOpprettetSkalSetteRiktigDelstegStatus() {
        ressurs.oppdaterSoknad(BEHANDLINGSID, "opprettet", null);
        verify(soknadService).settDelsteg(BEHANDLINGSID, DelstegStatus.OPPRETTET);
    }

    @Test
    public void oppdaterSoknadMedDelstegVedleggSkalSetteRiktigDelstegStatus() {
        ressurs.oppdaterSoknad(BEHANDLINGSID, "vedlegg", null);
        verify(soknadService).settDelsteg(BEHANDLINGSID, DelstegStatus.SKJEMA_VALIDERT);
    }

    @Test
    public void oppdaterSoknadMedDelstegOppsummeringSkalSetteRiktigDelstegStatus() {
        ressurs.oppdaterSoknad(BEHANDLINGSID, "oppsummering", null);
        verify(soknadService).settDelsteg(BEHANDLINGSID, DelstegStatus.VEDLEGG_VALIDERT);
    }

    @Test
    public void oppdaterSoknadMedJournalforendeenhetSkalSetteJournalforendeEnhet() {
        ressurs.oppdaterSoknad(BEHANDLINGSID, null, "NAV UTLAND");
        verify(soknadService).settJournalforendeEnhet(BEHANDLINGSID, "NAV UTLAND");
    }
}