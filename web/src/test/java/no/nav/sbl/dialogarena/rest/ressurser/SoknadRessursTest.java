package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.rest.meldinger.StartSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.XsrfGenerator;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;

import static no.nav.sbl.dialogarena.rest.ressurser.SoknadRessurs.XSRF_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SoknadRessursTest {

    public static final String BEHANDLINGSID = "123";

    @Mock
    SoknadService soknadService;

    @Mock
    XsrfGenerator xsrfGenerator;

    @InjectMocks
    SoknadRessurs ressurs;

    private StartSoknad type;

    @Before
    public void setUp() {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", StaticSubjectHandler.class.getName());
        type = new StartSoknad();
    }

    @Test
    public void hentingAvSoknadSkalSetteXsrfToken() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<Cookie> cookie = ArgumentCaptor.forClass(Cookie.class);
        ressurs.hentSoknadData(BEHANDLINGSID, response);
        verify(response).addCookie(cookie.capture());
        assertThat(cookie.getValue().getName()).isEqualTo(XSRF_TOKEN);
    }

    @Test
    public void opprettingAvSoknadSkalSetteXsrfToken() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<Cookie> cookie = ArgumentCaptor.forClass(Cookie.class);
        ressurs.opprettSoknad(null, type, response);
        verify(response).addCookie(cookie.capture());
        assertThat(cookie.getValue().getName()).isEqualTo(XSRF_TOKEN);
    }

    @Test
    public void opprettSoknadUtenBehandlingsidSkalStarteNySoknad() {
        ressurs.opprettSoknad(null, type, mock(HttpServletResponse.class));
        verify(soknadService).startSoknad(anyString());
    }

    @Test
    public void opprettSoknadMedBehandlingsidSomIkkeHarEttersendingSkalStarteNyEttersending() {
        when(soknadService.hentEttersendingForBehandlingskjedeId(BEHANDLINGSID)).thenReturn(null);
        ressurs.opprettSoknad(BEHANDLINGSID, type, mock(HttpServletResponse.class));
        verify(soknadService).startEttersending(eq(BEHANDLINGSID));
    }

    @Test
    public void opprettSoknadMedBehandlingsidSomHarEttersendingSkalIkkeStarteNyEttersending() {
        when(soknadService.hentEttersendingForBehandlingskjedeId(BEHANDLINGSID)).thenReturn(new WebSoknad());
        ressurs.opprettSoknad(BEHANDLINGSID, type, mock(HttpServletResponse.class));
        verify(soknadService, never()).startEttersending(eq(BEHANDLINGSID));
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